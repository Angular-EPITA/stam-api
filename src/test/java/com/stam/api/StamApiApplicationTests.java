package com.stam.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // On active le profil "test" pour utiliser application-test.yml
class StamApiApplicationTests {

    @Test
    void contextLoads() {
    }

}