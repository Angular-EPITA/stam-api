package com.stam.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI stamOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("STAM API - Catalogue de Jeux Vidéo")
                        .description("Documentation interactive de l'API REST STAM pour le MVP.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Angular")));
    }
}