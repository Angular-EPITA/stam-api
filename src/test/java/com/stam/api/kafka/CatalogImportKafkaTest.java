package com.stam.api.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stam.api.dto.GameRequestDTO;
import com.stam.api.repository.GameRepository;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=always",
    "spring.kafka.listener.auto-startup=true",
    "spring.kafka.consumer.auto-offset-reset=earliest"
})
class CatalogImportKafkaTest {

    static final String TOPIC = "stam.catalog.import.it";
    static final String GROUP_ID = "stam-it";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> GROUP_ID);
        registry.add("stam.kafka.catalog-import-topic", () -> TOPIC);
    }

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    GameRepository gameRepository;

    @Test
    void importAsync_publishesToKafka_andConsumerInsertsInDb() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        ensureTopicExists();

        long initialCount = gameRepository.count();

        GameRequestDTO dto = new GameRequestDTO();
        String title = "Kafka IT Game " + UUID.randomUUID();
        dto.setTitle(title);
        dto.setDescription("kafka integration test");
        dto.setReleaseDate(LocalDate.of(2024, 3, 3));
        dto.setPrice(9.99f);
        dto.setImageUrl("https://example.com/kafka.png");
        dto.setGenreId(1L);

        String responseBody = mockMvc.perform(post("/api/games/import-async")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(dto))))
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThat(responseBody).contains("1 jeu(x) envoyé(s) dans la file d'attente Kafka pour import.");

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    assertThat(gameRepository.count()).isEqualTo(initialCount + 1);
                    boolean found = gameRepository.findAll().stream().anyMatch(g -> title.equals(g.getTitle()));
                    assertThat(found).isTrue();
                });
    }

    private void ensureTopicExists() throws Exception {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());

        try (AdminClient admin = AdminClient.create(props)) {
            try {
                admin.createTopics(List.of(new NewTopic(TOPIC, 1, (short) 1))).all().get(10, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // topic already exists or auto-created
            }
        }
    }
}
