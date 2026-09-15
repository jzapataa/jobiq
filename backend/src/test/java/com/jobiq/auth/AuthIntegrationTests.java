package com.jobiq.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jobiq.shared.config.JwtProperties;
import com.jobiq.users.domain.User;
import com.jobiq.users.domain.UserRole;
import com.jobiq.users.repository.UserRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18.6")
            .withDatabaseName("jobiq")
            .withUsername("jobiq")
            .withPassword("jobiq-test");

    @Value("${local.server.port}")
    private int port;

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

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void candidateRegistrationNormalizesEmailAndStoresHashedPassword() throws Exception {
        HttpResponse<String> response = register(
                " CANDIDATE@Example.COM ",
                "candidate-password",
                "Candidate",
                "CANDIDATE");

        assertThat(response.statusCode()).isEqualTo(201);
        User stored = userRepository.findByEmail("candidate@example.com").orElseThrow();
        assertThat(stored.getRole()).isEqualTo(UserRole.CANDIDATE);
        assertThat(stored.getPasswordHash()).isNotEqualTo("candidate-password");
        assertThat(passwordEncoder.matches("candidate-password", stored.getPasswordHash())).isTrue();
    }

    @Test
    void recruiterRegistrationSucceeds() throws Exception {
        HttpResponse<String> response = register(
                "recruiter@example.com",
                "recruiter-password",
                "Recruiter",
                "RECRUITER");

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(userRepository.findByEmail("recruiter@example.com"))
                .get()
                .extracting(User::getRole)
                .isEqualTo(UserRole.RECRUITER);
    }

    @Test
    void duplicateEmailIsRejectedAfterNormalization() throws Exception {
        assertThat(register("candidate@example.com", "first-password", "Candidate", "CANDIDATE").statusCode())
                .isEqualTo(201);

        HttpResponse<String> duplicate = register(
                "CANDIDATE@example.com",
                "second-password",
                "Other",
                "RECRUITER");

        assertThat(duplicate.statusCode()).isEqualTo(409);
        assertThat(json(duplicate).get("code").asString()).isEqualTo("EMAIL_ALREADY_EXISTS");
    }

    @Test
    void loginSucceedsAndJwtContainsExpectedClaims() throws Exception {
        assertThat(register("candidate@example.com", "candidate-password", "Candidate", "CANDIDATE").statusCode())
                .isEqualTo(201);

        HttpResponse<String> login = login("candidate@example.com", "candidate-password");
        assertThat(login.statusCode()).isEqualTo(200);

        JsonNode payload = json(login);
        assertThat(payload.get("tokenType").asString()).isEqualTo("Bearer");
        assertThat(payload.get("expiresAt").asString()).isNotBlank();

        String token = payload.get("accessToken").asString();
        assertThat(token).isNotBlank();

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
        assertThat(register("candidate@example.com", "candidate-password", "Candidate", "CANDIDATE").statusCode())
                .isEqualTo(201);

        HttpResponse<String> wrongPassword = login("candidate@example.com", "wrong-password");
        HttpResponse<String> missingUser = login("missing@example.com", "wrong-password");

        assertThat(wrongPassword.statusCode()).isEqualTo(401);
        assertThat(json(wrongPassword).get("code").asString()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(missingUser.statusCode()).isEqualTo(401);
        assertThat(json(missingUser).get("code").asString()).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    void authMeWithoutTokenIsUnauthenticated() throws Exception {
        HttpResponse<String> response = get("/api/v1/auth/me", null);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(json(response).get("code").asString()).isEqualTo("UNAUTHENTICATED");
    }

    @Test
    void authMeWithValidTokenReturnsUserAndTransitionalIncompleteProfile() throws Exception {
        assertThat(register("candidate@example.com", "candidate-password", "Candidate", "CANDIDATE").statusCode())
                .isEqualTo(201);
        String token = loginToken("candidate@example.com", "candidate-password");

        HttpResponse<String> response = get("/api/v1/auth/me", token);
        JsonNode payload = json(response);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(payload.get("email").asString()).isEqualTo("candidate@example.com");
        assertThat(payload.get("role").asString()).isEqualTo("CANDIDATE");
        assertThat(payload.get("profileComplete").asBoolean()).isFalse();
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

        HttpResponse<String> response = get("/api/v1/auth/me", expiredToken);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(json(response).get("code").asString()).isEqualTo("UNAUTHENTICATED");
    }

    @Test
    void authEndpointsRemainPublicWhileProtectedEndpointRequiresAuthentication() throws Exception {
        HttpResponse<String> register = postJson("/api/v1/auth/register", "{}");
        HttpResponse<String> login = postJson("/api/v1/auth/login", "{}");
        HttpResponse<String> me = get("/api/v1/auth/me", null);

        assertThat(register.statusCode()).isEqualTo(400);
        assertThat(json(register).get("code").asString()).isEqualTo("VALIDATION_ERROR");
        assertThat(login.statusCode()).isEqualTo(400);
        assertThat(json(login).get("code").asString()).isEqualTo("VALIDATION_ERROR");
        assertThat(me.statusCode()).isEqualTo(401);
    }

    private HttpResponse<String> register(String email, String password, String name, String role)
            throws IOException, InterruptedException {
        String body = """
                {"email":"%s","password":"%s","name":"%s","role":"%s"}
                """.formatted(email, password, name, role);
        return postJson("/api/v1/auth/register", body);
    }

    private HttpResponse<String> login(String email, String password) throws IOException, InterruptedException {
        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
        return postJson("/api/v1/auth/login", body);
    }

    private String loginToken(String email, String password) throws Exception {
        HttpResponse<String> response = login(email, password);
        assertThat(response.statusCode()).isEqualTo(200);
        return json(response).get("accessToken").asString();
    }

    private HttpResponse<String> postJson(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String accessToken) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri(path))
                .GET();
        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode json(HttpResponse<String> response) {
        return jsonMapper.readTree(response.body());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
