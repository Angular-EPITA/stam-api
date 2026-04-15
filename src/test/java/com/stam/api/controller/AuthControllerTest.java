package com.stam.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stam.api.dto.AuthRequestDTO;
import com.stam.api.dto.RefreshRequestDTO;
import com.stam.api.security.JwtService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
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
class AuthControllerTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired JwtService jwtService;
    @Autowired UserDetailsService userDetailsService;

    MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void login_validCredentials_returnsTokens() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("admin");
        request.setPassword("admin");

        String body = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andReturn().getResponse().getContentAsString();

        String accessToken = JsonPath.read(body, "$.accessToken");
        String username = jwtService.extractUsername(accessToken);
        assertThat(username).isEqualTo("admin");
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("admin");
        request.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_validToken_returnsNewTokens() throws Exception {
        UserDetails admin = userDetailsService.loadUserByUsername("admin");
        String refreshToken = jwtService.generateRefreshToken(admin);

        RefreshRequestDTO request = new RefreshRequestDTO();
        request.setRefreshToken(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void refresh_withAccessToken_returns401() throws Exception {
        UserDetails admin = userDetailsService.loadUserByUsername("admin");
        String accessToken = jwtService.generateAccessToken(admin);

        RefreshRequestDTO request = new RefreshRequestDTO();
        request.setRefreshToken(accessToken);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
}
