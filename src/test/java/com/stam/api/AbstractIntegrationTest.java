package com.stam.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    // On simule (mock) Kafka pour que les tests normaux 
    // ne cherchent pas à se connecter à un vrai serveur !
    @MockitoBean
    protected KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    protected KafkaAdmin kafkaAdmin;
}