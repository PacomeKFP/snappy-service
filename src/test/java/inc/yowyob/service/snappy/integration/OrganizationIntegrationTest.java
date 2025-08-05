package inc.yowyob.service.snappy.integration;

import inc.yowyob.service.snappy.domain.entities.Organization;
import inc.yowyob.service.snappy.infrastructure.repositories.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class OrganizationIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OrganizationRepository organizationRepository;

    @BeforeEach
    void setUp() {
        // Clean up the database before each test
        organizationRepository.deleteAll().block();
    }

    @Test
    void testGetAllOrganizations_EmptyDatabase() {
        webTestClient.get()
                .uri("/organizations/getAll/password")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Organization.class)
                .hasSize(0);
    }

    @Test
    void testGetAllOrganizations_WithData() {
        // Create test organization using constructor
        Organization testOrg = new Organization("Test Organization", "test@example.com", "password123");
        testOrg.setProjectId("test-project");

        // Save it to the database
        StepVerifier.create(organizationRepository.save(testOrg))
                .expectNextMatches(saved -> saved.getId() != null && saved.getName().equals("Test Organization"))
                .verifyComplete();

        // Test the endpoint
        webTestClient.get()
                .uri("/organizations/getAll/password")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Organization.class)
                .hasSize(1)
                .value(orgs -> {
                    Organization org = orgs.get(0);
                    assert org.getName().equals("Test Organization");
                    assert org.getEmail().equals("test@example.com");
                });
    }

    @Test
    void testGetAllOrganizations_WrongKey() {
        webTestClient.get()
                .uri("/organizations/getAll/wrongkey")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Organization.class)
                .hasSize(0);
    }

    @Test
    void testCreateOrganization() {
        String createOrgJson = """
                {
                    "name": "New Organization",
                    "email": "new@example.com",
                    "password": "newpassword"
                }
                """;

        webTestClient.post()
                .uri("/organizations")
                .header("Content-Type", "application/json")
                .bodyValue(createOrgJson)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Organization.class)
                .value(org -> {
                    assert org.getName().equals("New Organization");
                    assert org.getEmail().equals("new@example.com");
                    assert org.getId() != null;
                });
    }
}