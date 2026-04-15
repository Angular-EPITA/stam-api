package com.stam.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=always",
    "spring.kafka.listener.auto-startup=false"
})
class StamApiApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }

}