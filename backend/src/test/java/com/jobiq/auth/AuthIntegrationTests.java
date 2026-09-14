package com.jobiq.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jobiq.shared.config.JwtProperties;
import com.jobiq.users.domain.User;
import com.jobiq.users.domain.UserRole;
import com.jobiq.users.repository.UserRepository;

import tools.jackson.databind.json.JsonMapper;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18.6")
            .withDatabaseName("jobiq")
            .withUsername("jobiq")
            .withPassword("jobiq-test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void candidateRegistrationNormalizesEmailAndStoresHashedPassword() throws Exception {
        register(" CANDIDATE@Example.COM ", "candidate-password", "Candidate", "CANDIDATE")
                .andExpect(status().isCreated());

        User stored = userRepository.findByEmail("candidate@example.com").orElseThrow();
        assertThat(stored.getRole()).isEqualTo(UserRole.CANDIDATE);
        assertThat(stored.getPasswordHash()).isNotEqualTo("candidate-password");
        assertThat(passwordEncoder.matches("candidate-password", stored.getPasswordHash())).isTrue();
    }

    @Test
    void recruiterRegistrationSucceeds() throws Exception {
        register("recruiter@example.com", "recruiter-password", "Recruiter", "RECRUITER")
                .andExpect(status().isCreated());

        assertThat(userRepository.findByEmail("recruiter@example.com"))
                .get()
                .extracting(User::getRole)
                .isEqualTo(UserRole.RECRUITER);
    }

    @Test
    void duplicateEmailIsRejectedAfterNormalization() throws Exception {
        register("candidate@example.com", "first-password", "Candidate", "CANDIDATE")
                .andExpect(status().isCreated());

        register("CANDIDATE@example.com", "second-password", "Other", "RECRUITER")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void loginSucceedsAndJwtContainsExpectedClaims() throws Exception {
        register("candidate@example.com", "candidate-password", "Candidate", "CANDIDATE")
                .andExpect(status().isCreated());

        MvcResult result = login("candidate@example.com", "candidate-password")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andReturn();

        String token = jsonMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken")
                .asString();
        Jwt jwt = jwtDecoder.decode(token);
        User user = userRepository.findByEmail("candidate@example.com").orElseThrow();

        assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
        assertThat(jwt.getClaimAsString("role")).isEqualTo("CANDIDATE");
        assertThat(jwt.getIssuer()).hasToString(jwtProperties.issuer());
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
    }

    @Test
    void wrongPasswordReturnsInvalidCredentialsWithoutAccountEnumeration() throws Exception {
        register("candidate@example.com", "candidate-password", "Candidate", "CANDIDATE")
                .andExpect(status().isCreated());

        login("candidate@example.com", "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

        login("missing@example.com", "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void authMeWithoutTokenIsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void authMeWithValidTokenReturnsUserAndTransitionalIncompleteProfile() throws Exception {
        register("candidate@example.com", "candidate-password", "Candidate", "CANDIDATE")
                .andExpect(status().isCreated());
        String token = loginToken("candidate@example.com", "candidate-password");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("candidate@example.com"))
                .andExpect(jsonPath("$.role").value("CANDIDATE"))
                .andExpect(jsonPath("$.profileComplete").value(false));
    }

    @Test
    void expiredJwtIsRejected() throws Exception {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(UUID.randomUUID().toString())
                .issuedAt(now.minusSeconds(3600))
                .expiresAt(now.minusSeconds(1))
                .claim("role", "CANDIDATE")
                .build();
        String expiredToken = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims)).getTokenValue();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void authEndpointsRemainPublicWhileProtectedEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions register(
            String email,
            String password,
            String name,
            String role) throws Exception {
        String body = """
                {"email":"%s","password":"%s","name":"%s","role":"%s"}
                """.formatted(email, password, name, role);
        return mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private org.springframework.test.web.servlet.ResultActions login(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private String loginToken(String email, String password) throws Exception {
        MvcResult result = login(email, password)
                .andExpect(status().isOk())
                .andReturn();
        return jsonMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken")
                .asString();
    }
}
