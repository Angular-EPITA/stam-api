package com.stam.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    JwtService jwtService;
    UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
                "dGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNlY3JldCBrZXkgZm9yIEhNQUMgU0hBIDI1NiBzaWduaW5n");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 300_000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604_800_000L);

        userDetails = new User("admin", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void generateAccessToken_andExtractUsername() {
        String token = jwtService.generateAccessToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    void accessToken_isValid() {
        String token = jwtService.generateAccessToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        assertThat(jwtService.isRefreshToken(token)).isFalse();
    }

    @Test
    void refreshToken_isValid_andMarkedAsRefresh() {
        String token = jwtService.generateRefreshToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        assertThat(jwtService.isRefreshToken(token)).isTrue();
    }

    @Test
    void token_invalidForDifferentUser() {
        String token = jwtService.generateAccessToken(userDetails);

        UserDetails other = new User("other", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void expiredToken_isInvalid() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1000L);

        String token = jwtService.generateAccessToken(userDetails);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, userDetails))
                .isInstanceOf(Exception.class);
    }

    @Test
    void malformedToken_throwsException() {
        assertThatThrownBy(() -> jwtService.extractUsername("not.a.valid.jwt"))
                .isInstanceOf(Exception.class);
    }
}
