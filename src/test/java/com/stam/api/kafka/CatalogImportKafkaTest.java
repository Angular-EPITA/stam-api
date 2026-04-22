package com.stam.api.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stam.api.dto.GameRequestDTO;
import com.stam.api.kafka.dto.PartnerCatalogImportMessage; // <-- IMPORT AJOUTÉ
import com.stam.api.repository.GameRepository;
import com.stam.api.security.JwtService;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1)
@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "spring.sql.init.mode=always",
    "spring.kafka.listener.auto-startup=true",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "spring.kafka.listener.shutdown-timeout=0",
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "logging.level.org.apache.kafka=WARN",
    "logging.level.kafka=WARN"
})
class CatalogImportKafkaTest {
    static final String TOPIC = "stam.catalog.import.it";
    static final String GROUP_ID = "stam-it";

    @DynamicPropertySource
    static void registerKafkaTopics(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.consumer.group-id", () -> GROUP_ID);
        registry.add("stam.kafka.catalog-import-topic", () -> TOPIC);
    }

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired GameRepository gameRepository;
    @Autowired JwtService jwtService;
    @Autowired UserDetailsService userDetailsService;
    @Autowired(required = false) KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    
    @Autowired EmbeddedKafkaBroker embeddedKafkaBroker; 

    MockMvc mockMvc;
    String adminToken;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
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
    }

    @Test
    void importAsync_publishesToKafka_andConsumerInsertsInDb() throws Exception {
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

        // CORRECTION ICI : On utilise le bon objet attendu par le contrôleur
        PartnerCatalogImportMessage payload = new PartnerCatalogImportMessage();
        payload.setPartnerName("Ubisoft IT");
        payload.setGames(List.of(dto));

        // CORRECTION ICI : On utilise la bonne URL "/api/partners/catalog"
        mockMvc.perform(post("/api/partners/catalog")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isAccepted());

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
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());

        try (AdminClient admin = AdminClient.create(props)) {
            try {
                admin.createTopics(List.of(new NewTopic(TOPIC, 1, (short) 1))).all().get(10, TimeUnit.SECONDS);
            } catch (Exception ignored) {}
        }
    }
}
