package com.jobiq;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class JobiqApplicationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18.6")
            .withDatabaseName("jobiq")
            .withUsername("jobiq")
            .withPassword("jobiq-test");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextStartsWithPostgreSql() {
        assertThat(POSTGRES.isRunning()).isTrue();
    }

    @Test
    void livenessIsPublicAndGenericHealthIsNotPublic() {
        ResponseEntity<String> liveness = restTemplate.getForEntity("/actuator/health/liveness", String.class);
        ResponseEntity<String> genericHealth = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(liveness.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(liveness.getBody()).contains("\"status\":\"UP\"");
        assertThat(liveness.getBody()).doesNotContain("db");
        assertThat(genericHealth.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
