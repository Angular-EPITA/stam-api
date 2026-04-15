package com.stam.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stam.api.AbstractIntegrationTest;
import com.stam.api.dto.GameRequestDTO;
import com.stam.api.security.JwtService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "spring.sql.init.mode=always",
    "spring.kafka.listener.auto-startup=false",
    "logging.level.org.apache.kafka=WARN",
    "logging.level.kafka=WARN",
    "logging.level.org.testcontainers=WARN",
    "logging.level.com.github.dockerjava=WARN",
    "logging.level.org.hibernate.SQL=WARN"
})
class GameControllerCrudTest extends AbstractIntegrationTest {

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserDetailsService userDetailsService;

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

    @Test
    void publicEndpoint_games_returns200WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/games?page=0&size=5"))
            .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_returns401WithoutAuth() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        GameRequestDTO createDto = new GameRequestDTO();
        createDto.setTitle("Unauthorized Game");
        createDto.setDescription("should fail");
        createDto.setReleaseDate(LocalDate.of(2024, 1, 1));
        createDto.setPrice(9.99f);
        createDto.setImageUrl("https://example.com/game.png");
        createDto.setGenreId(1L);

        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isForbidden());
    }

    @Test
    void crud_create_get_update_delete() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        GameRequestDTO createDto = new GameRequestDTO();
        String createTitle = "IT Game " + UUID.randomUUID();
        createDto.setTitle(createTitle);
        createDto.setDescription("integration test");
        createDto.setReleaseDate(LocalDate.of(2024, 1, 1));
        createDto.setPrice(19.99f);
        createDto.setImageUrl("https://example.com/game.png");
        createDto.setGenreId(1L);

        String createBody = mockMvc.perform(post("/api/games")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.title").value(createTitle))
            .andExpect(jsonPath("$.genre.id").value(1))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String createdId = JsonPath.read(createBody, "$.id");
        assertThat(createdId).isNotBlank();

        mockMvc.perform(get("/api/games/{id}", createdId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(createdId));

        GameRequestDTO updateDto = new GameRequestDTO();
        updateDto.setTitle(createDto.getTitle() + " updated");
        updateDto.setDescription("updated desc");
        updateDto.setReleaseDate(LocalDate.of(2024, 2, 2));
        updateDto.setPrice(29.99f);
        updateDto.setImageUrl("https://example.com/game2.png");
        updateDto.setGenreId(2L);

        mockMvc.perform(put("/api/games/{id}", createdId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value(updateDto.getTitle()))
            .andExpect(jsonPath("$.genre.id").value(2));

        mockMvc.perform(delete("/api/games/{id}", createdId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/games/{id}", createdId))
            .andExpect(status().isNotFound());
    }
}
