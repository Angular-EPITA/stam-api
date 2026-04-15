package com.stam.api.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stam.api.AbstractIntegrationTest;
import com.stam.api.dto.GameRequestDTO;
import com.stam.api.repository.GameRepository;
import com.stam.api.security.JwtService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
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
class CatalogImportKafkaTest extends AbstractIntegrationTest {

    static final String TOPIC = "stam.catalog.import.it";
    static final String DLT_TOPIC = TOPIC + ".dlt";
    static final String GROUP_ID = "stam-it";

    @DynamicPropertySource
    static void registerKafkaTopics(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.consumer.group-id", () -> GROUP_ID);
        registry.add("stam.kafka.catalog-import-topic", () -> TOPIC);
    }

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    GameRepository gameRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired(required = false)
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

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
                if (!container.isRunning()) {
                    continue;
                }
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

        String partnerId = "partner-it";

        mockMvc.perform(post("/api/partners/" + partnerId + "/catalog/import-async")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(dto))))
            .andExpect(status().isAccepted())
            .andReturn();

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    assertThat(gameRepository.count()).isEqualTo(initialCount + 1);
                    boolean found = gameRepository.findAll().stream().anyMatch(g -> title.equals(g.getTitle()));
                    assertThat(found).isTrue();
                });
    }

    @Test
    void invalidKafkaMessage_isSentToDlt() throws Exception {
        ensureTopicExists();

        // envoie un message volontairement invalide (pas du JSON)
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            producer.send(new ProducerRecord<>(TOPIC, "bad-" + Instant.now().toEpochMilli(), "not-json"));
            producer.flush();
        }

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "dlt-it-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(List.of(DLT_TOPIC));

            Awaitility.await()
                    .atMost(Duration.ofSeconds(20))
                    .pollInterval(Duration.ofMillis(250))
                    .untilAsserted(() -> {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                        assertThat(records.count()).isGreaterThan(0);
                        ConsumerRecord<String, String> record = records.iterator().next();
                        assertThat(record.value()).isEqualTo("not-json");
                    });
        }
    }

    private void ensureTopicExists() throws Exception {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());

        try (AdminClient admin = AdminClient.create(props)) {
            try {
                admin.createTopics(List.of(
                        new NewTopic(TOPIC, 1, (short) 1)
                )).all().get(10, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // topic already exists or auto-created
            }
        }
    }
}
