package com.stam.api.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stam.api.dto.GameRequestDTO;
import com.stam.api.kafka.dto.GameEventMessage;
import com.stam.api.repository.GameRepository;
import com.stam.api.security.JwtService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
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
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "spring.sql.init.mode=always",
    "spring.kafka.listener.auto-startup=true",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "spring.kafka.listener.shutdown-timeout=0",
    "logging.level.org.apache.kafka=WARN",
    "logging.level.kafka=WARN",
    "logging.level.org.testcontainers=WARN",
    "logging.level.com.github.dockerjava=WARN",
    "logging.level.org.hibernate.SQL=WARN"
})
class GameEventKafkaTest {

    static final String GAME_EVENTS_TOPIC = "game.events.it";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        if (!kafka.isRunning()) {
            kafka.start();
        }
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "stam-game-events-it");
        registry.add("stam.kafka.game-events-topic", () -> GAME_EVENTS_TOPIC);
        registry.add("stam.kafka.catalog-import-topic", () -> "catalog.import.it.unused");
    }

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired GameRepository gameRepository;
    @Autowired JwtService jwtService;
    @Autowired UserDetailsService userDetailsService;
    @Autowired GameEventConsumer gameEventConsumer;
    @Autowired(required = false) KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    MockMvc mockMvc;
    String adminToken;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        UserDetails admin = userDetailsService.loadUserByUsername("admin");
        adminToken = jwtService.generateAccessToken(admin);
    }

    @AfterAll
    void stopKafkaListeners() throws InterruptedException {
        if (kafkaListenerEndpointRegistry != null) {
            for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
                if (!container.isRunning()) continue;
                CountDownLatch latch = new CountDownLatch(1);
                container.stop(latch::countDown);
                latch.await(5, TimeUnit.SECONDS);
            }
        }
        if (kafka.isRunning()) kafka.stop();
    }

    @Test
    void createGame_publishesCreatedEvent_andConsumerReceives() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        GameRequestDTO dto = new GameRequestDTO();
        String title = "Kafka Event Test " + UUID.randomUUID();
        dto.setTitle(title);
        dto.setDescription("test game event flow");
        dto.setReleaseDate(LocalDate.of(2025, 6, 15));
        dto.setPrice(19.99f);
        dto.setImageUrl("https://example.com/event-test.png");
        dto.setGenreId(1L);

        mockMvc.perform(post("/api/games")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated());

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    List<GameEventMessage> events = gameEventConsumer.getRecentEvents();
                    assertThat(events).anyMatch(e ->
                            "CREATED".equals(e.getEventType()) && title.equals(e.getGameTitle()));
                });
    }

    @Test
    void deleteGame_publishesDeletedEvent_andConsumerReceives() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        GameRequestDTO dto = new GameRequestDTO();
        String title = "Kafka Delete Test " + UUID.randomUUID();
        dto.setTitle(title);
        dto.setDescription("test kafka delete event");
        dto.setReleaseDate(LocalDate.of(2025, 7, 1));
        dto.setPrice(29.99f);
        dto.setImageUrl("https://example.com/delete-test.png");
        dto.setGenreId(1L);

        String createBody = mockMvc.perform(post("/api/games")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String gameId = com.jayway.jsonpath.JsonPath.read(createBody, "$.id");

        mockMvc.perform(delete("/api/games/{id}", gameId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    List<GameEventMessage> events = gameEventConsumer.getRecentEvents();
                    assertThat(events).anyMatch(e ->
                            "DELETED".equals(e.getEventType()) && title.equals(e.getGameTitle()));
                });
    }
}
