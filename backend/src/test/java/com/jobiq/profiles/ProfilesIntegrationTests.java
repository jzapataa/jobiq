package com.jobiq.profiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jobiq.candidates.repository.CandidateProfileRepository;
import com.jobiq.candidates.repository.CandidateSkillRepository;
import com.jobiq.companies.dto.CompanyRequest;
import com.jobiq.companies.repository.CompanyRepository;
import com.jobiq.recruiters.dto.RecruiterProfileRequest;
import com.jobiq.recruiters.repository.RecruiterProfileRepository;
import com.jobiq.recruiters.service.RecruiterProfileService;
import com.jobiq.skills.domain.Skill;
import com.jobiq.skills.repository.SkillRepository;
import com.jobiq.skills.service.SkillResolver;
import com.jobiq.users.repository.UserRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProfilesIntegrationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18.6")
            .withDatabaseName("jobiq")
            .withUsername("jobiq")
            .withPassword("jobiq-test");

    @Value("${local.server.port}")
    private int port;

    @Autowired CandidateSkillRepository candidateSkillRepository;
    @Autowired CandidateProfileRepository candidateProfileRepository;
    @Autowired RecruiterProfileRepository recruiterProfileRepository;
    @Autowired CompanyRepository companyRepository;
    @Autowired SkillRepository skillRepository;
    @Autowired UserRepository userRepository;
    @Autowired RecruiterProfileService recruiterProfileService;
    @Autowired SkillResolver skillResolver;
    @Autowired JsonMapper jsonMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void cleanDatabase() {
        candidateSkillRepository.deleteAll();
        recruiterProfileRepository.deleteAll();
        candidateProfileRepository.deleteAll();
        companyRepository.deleteAll();
        skillRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void candidateProfileLifecycleUsesFullReplacementAndRealProfileComplete() throws Exception {
        register("candidate@example.com", "CANDIDATE");
        String token = loginToken("candidate@example.com");

        HttpResponse<String> before = get("/api/v1/candidate/profile", token);
        assertThat(before.statusCode()).isEqualTo(404);
        assertThat(json(before).get("code").asString()).isEqualTo("PROFILE_NOT_FOUND");
        assertThat(profileComplete(token)).isFalse();

        String firstBody = """
                {
                  "headline":"Java Software Engineer",
                  "location":"Murcia",
                  "bio":"Backend engineer",
                  "linkedinUrl":"https://linkedin.example/candidate",
                  "githubUrl":"https://github.example/candidate",
                  "portfolioUrl":"https://portfolio.example/candidate",
                  "skills":["Java"," Spring Boot ","java","PostgreSQL"]
                }
                """;
        HttpResponse<String> created = putJson("/api/v1/candidate/profile", firstBody, token);
        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(profileComplete(token)).isTrue();
        assertThat(skillRepository.count()).isEqualTo(3);
        assertThat(candidateSkillRepository.count()).isEqualTo(3);

        String secondBody = """
                {
                  "headline":"Senior Java Engineer",
                  "location":"Murcia",
                  "bio":"Updated bio",
                  "linkedinUrl":null,
                  "githubUrl":"https://github.example/candidate",
                  "portfolioUrl":null,
                  "skills":["JAVA","Kotlin"]
                }
                """;
        HttpResponse<String> updated = putJson("/api/v1/candidate/profile", secondBody, token);
        assertThat(updated.statusCode()).isEqualTo(200);
        assertThat(candidateSkillRepository.count()).isEqualTo(2);
        assertThat(skillRepository.count()).isEqualTo(4);

        HttpResponse<String> loaded = get("/api/v1/candidate/profile", token);
        JsonNode payload = json(loaded);
        assertThat(loaded.statusCode()).isEqualTo(200);
        assertThat(payload.get("headline").asString()).isEqualTo("Senior Java Engineer");
        assertThat(payload.get("bio").asString()).isEqualTo("Updated bio");
        assertThat(payload.get("linkedinUrl").isNull()).isTrue();
        assertThat(payload.get("portfolioUrl").isNull()).isTrue();
        assertThat(payload.get("skills").size()).isEqualTo(2);
        assertThat(skillRepository.findByNormalizedName("java")).isPresent();
        assertThat(skillRepository.findByNormalizedName("kotlin")).isPresent();
    }

    @Test
    void candidateValidationAndRoleIsolationAreEnforced() throws Exception {
        register("candidate@example.com", "CANDIDATE");
        register("recruiter@example.com", "RECRUITER");
        String candidateToken = loginToken("candidate@example.com");
        String recruiterToken = loginToken("recruiter@example.com");

        HttpResponse<String> invalid = putJson("/api/v1/candidate/profile", """
                {"headline":" ","location":"Murcia","bio":"Bio","skills":[]}
                """, candidateToken);
        assertThat(invalid.statusCode()).isEqualTo(400);
        assertThat(json(invalid).get("code").asString()).isEqualTo("VALIDATION_ERROR");

        assertThat(get("/api/v1/candidate/profile", recruiterToken).statusCode()).isEqualTo(403);
        assertThat(get("/api/v1/recruiter/profile", candidateToken).statusCode()).isEqualTo(403);
        assertThat(get("/api/v1/candidate/profile", null).statusCode()).isEqualTo(401);
    }

    @Test
    void candidateEndpointsCanOnlyOperateOnAuthenticatedUsersOwnProfile() throws Exception {
        register("one@example.com", "CANDIDATE");
        register("two@example.com", "CANDIDATE");
        String one = loginToken("one@example.com");
        String two = loginToken("two@example.com");

        assertThat(putJson("/api/v1/candidate/profile", candidateBody("One", List.of("Java")), one).statusCode())
                .isEqualTo(201);
        assertThat(putJson("/api/v1/candidate/profile", candidateBody("Two", List.of("Kotlin")), two).statusCode())
                .isEqualTo(201);

        assertThat(json(get("/api/v1/candidate/profile", one)).get("headline").asString()).isEqualTo("One");
        assertThat(json(get("/api/v1/candidate/profile", two)).get("headline").asString()).isEqualTo("Two");
    }

    @Test
    void concurrentSkillResolutionReusesOneCanonicalSkill() throws Exception {
        int workers = 12;
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<UUID>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < workers; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    Skill skill = skillResolver.resolveAll(List.of("  Java  ")).getFirst();
                    return skill.getId();
                }));
            }
            start.countDown();
            Set<UUID> ids = new java.util.HashSet<>();
            for (Future<UUID> future : futures) {
                ids.add(future.get());
            }
            assertThat(ids).hasSize(1);
            assertThat(skillRepository.count()).isEqualTo(1);
            assertThat(skillRepository.findByNormalizedName("java")).isPresent();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void recruiterOnboardingIsAtomicAndCompanyProfileEditingIsSeparated() throws Exception {
        register("recruiter@example.com", "RECRUITER");
        String token = loginToken("recruiter@example.com");

        HttpResponse<String> before = get("/api/v1/recruiter/profile", token);
        assertThat(before.statusCode()).isEqualTo(404);
        assertThat(json(before).get("code").asString()).isEqualTo("PROFILE_NOT_FOUND");
        assertThat(profileComplete(token)).isFalse();

        HttpResponse<String> created = putJson("/api/v1/recruiter/profile", """
                {
                  "position":"Technical Recruiter",
                  "company":{
                    "name":"Example Company",
                    "description":"Technology company",
                    "website":"https://example.test",
                    "logoUrl":null,
                    "location":"Murcia"
                  }
                }
                """, token);
        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(companyRepository.count()).isEqualTo(1);
        assertThat(recruiterProfileRepository.count()).isEqualTo(1);
        assertThat(profileComplete(token)).isTrue();

        HttpResponse<String> positionUpdate = putJson("/api/v1/recruiter/profile", """
                {"position":"Senior Technical Recruiter"}
                """, token);
        assertThat(positionUpdate.statusCode()).isEqualTo(200);
        assertThat(json(positionUpdate).get("position").asString()).isEqualTo("Senior Technical Recruiter");
        assertThat(companyRepository.count()).isEqualTo(1);

        HttpResponse<String> companyGet = get("/api/v1/recruiter/company", token);
        assertThat(companyGet.statusCode()).isEqualTo(200);
        assertThat(json(companyGet).get("name").asString()).isEqualTo("Example Company");

        HttpResponse<String> companyUpdate = putJson("/api/v1/recruiter/company", """
                {
                  "name":"Example Company Updated",
                  "description":"Updated description",
                  "website":null,
                  "logoUrl":null,
                  "location":"Madrid"
                }
                """, token);
        assertThat(companyUpdate.statusCode()).isEqualTo(200);
        assertThat(json(companyUpdate).get("name").asString()).isEqualTo("Example Company Updated");
        assertThat(json(companyUpdate).get("website").isNull()).isTrue();
    }

    @Test
    void recruiterInitialPutRequiresCompanyAndArbitraryCompanyAttachmentIsImpossible() throws Exception {
        register("recruiter@example.com", "RECRUITER");
        String token = loginToken("recruiter@example.com");

        HttpResponse<String> noCompany = putJson("/api/v1/recruiter/profile", """
                {"position":"Technical Recruiter","companyId":"00000000-0000-0000-0000-000000000001"}
                """, token);
        assertThat(noCompany.statusCode()).isEqualTo(400);
        assertThat(json(noCompany).get("code").asString()).isEqualTo("VALIDATION_ERROR");
        assertThat(companyRepository.count()).isZero();
        assertThat(recruiterProfileRepository.count()).isZero();
    }

    @Test
    void failedRecruiterCreationRollsBackCompany() {
        long companiesBefore = companyRepository.count();
        RecruiterProfileRequest request = new RecruiterProfileRequest(
                "Technical Recruiter",
                new CompanyRequest("Rollback Company", "Description", null, null, "Murcia"));

        assertThatThrownBy(() -> recruiterProfileService.putOwnProfile(UUID.randomUUID(), request))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(companyRepository.count()).isEqualTo(companiesBefore);
        assertThat(recruiterProfileRepository.count()).isZero();
    }

    @Test
    void candidateCannotUseRecruiterEndpointsAndRecruiterCannotUseCandidateEndpoints() throws Exception {
        register("candidate@example.com", "CANDIDATE");
        register("recruiter@example.com", "RECRUITER");
        String candidate = loginToken("candidate@example.com");
        String recruiter = loginToken("recruiter@example.com");

        assertThat(get("/api/v1/recruiter/company", candidate).statusCode()).isEqualTo(403);
        assertThat(putJson("/api/v1/recruiter/profile", "{\"position\":\"Recruiter\"}", candidate).statusCode())
                .isEqualTo(403);
        assertThat(putJson("/api/v1/candidate/profile", candidateBody("Wrong", List.of()), recruiter).statusCode())
                .isEqualTo(403);
    }

    private boolean profileComplete(String token) throws Exception {
        HttpResponse<String> me = get("/api/v1/auth/me", token);
        assertThat(me.statusCode()).isEqualTo(200);
        return json(me).get("profileComplete").asBoolean();
    }

    private String candidateBody(String headline, List<String> skills) {
        String skillsJson = skills.stream().map(value -> "\"" + value + "\"").collect(java.util.stream.Collectors.joining(","));
        return """
                {"headline":"%s","location":"Murcia","bio":"Bio","linkedinUrl":null,"githubUrl":null,"portfolioUrl":null,"skills":[%s]}
                """.formatted(headline, skillsJson);
    }

    private void register(String email, String role) throws Exception {
        String body = """
                {"email":"%s","password":"password-123","name":"Test User","role":"%s"}
                """.formatted(email, role);
        assertThat(postJson("/api/v1/auth/register", body).statusCode()).isEqualTo(201);
    }

    private String loginToken(String email) throws Exception {
        HttpResponse<String> response = postJson("/api/v1/auth/login", """
                {"email":"%s","password":"password-123"}
                """.formatted(email));
        assertThat(response.statusCode()).isEqualTo(200);
        return json(response).get("accessToken").asString();
    }

    private HttpResponse<String> postJson(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(uri(path)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> putJson(String path, String body, String token) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri(path)).header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String token) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri(path)).GET();
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode json(HttpResponse<String> response) {
        return jsonMapper.readTree(response.body());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
