package com.stam.api.service;

import com.stam.api.dto.GameRequestDTO;
import com.stam.api.entity.Game;
import com.stam.api.entity.Genre;
import com.stam.api.kafka.GameEventProducer;
import com.stam.api.repository.GameRepository;
import com.stam.api.repository.GenreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock GameRepository gameRepository;
    @Mock GenreRepository genreRepository;
    @Mock GameEventProducer gameEventProducer;

    @InjectMocks GameService gameService;

    @Test
    void createGame_validDto_savesAndPublishesEvent() {
        Genre genre = new Genre(1L, "Action");
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));

        Game saved = new Game();
        saved.setId(UUID.randomUUID());
        saved.setTitle("Test Game");
        saved.setGenre(genre);
        when(gameRepository.save(any(Game.class))).thenReturn(saved);

        GameRequestDTO dto = new GameRequestDTO();
        dto.setTitle("Test Game");
        dto.setDescription("desc");
        dto.setReleaseDate(LocalDate.of(2025, 1, 1));
        dto.setPrice(19.99f);
        dto.setImageUrl("https://img.com/a.png");
        dto.setGenreId(1L);

        Game result = gameService.createGame(dto);

        assertThat(result.getTitle()).isEqualTo("Test Game");
        verify(gameRepository).save(any(Game.class));
        verify(gameEventProducer).publish(eq("CREATED"), eq(saved.getId()), eq("Test Game"));
    }

    @Test
    void createGame_unknownGenre_throws400() {
        when(genreRepository.findById(999L)).thenReturn(Optional.empty());

        GameRequestDTO dto = new GameRequestDTO();
        dto.setTitle("X");
        dto.setGenreId(999L);

        assertThatThrownBy(() -> gameService.createGame(dto))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateGame_validDto_updatesAndPublishesEvent() {
        UUID id = UUID.randomUUID();
        Genre genre = new Genre(2L, "RPG");
        Game existing = new Game();
        existing.setId(id);
        existing.setTitle("Old");

        when(gameRepository.findById(id)).thenReturn(Optional.of(existing));
        when(genreRepository.findById(2L)).thenReturn(Optional.of(genre));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        GameRequestDTO dto = new GameRequestDTO();
        dto.setTitle("Updated");
        dto.setDescription("new desc");
        dto.setReleaseDate(LocalDate.of(2025, 6, 1));
        dto.setPrice(29.99f);
        dto.setImageUrl("https://img.com/b.png");
        dto.setGenreId(2L);

        Game result = gameService.updateGame(id, dto);

        assertThat(result.getTitle()).isEqualTo("Updated");
        verify(gameEventProducer).publish(eq("UPDATED"), eq(id), eq("Updated"));
    }

    @Test
    void updateGame_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(gameRepository.findById(id)).thenReturn(Optional.empty());

        GameRequestDTO dto = new GameRequestDTO();
        dto.setTitle("X");
        dto.setGenreId(1L);

        assertThatThrownBy(() -> gameService.updateGame(id, dto))
                .isInstanceOf(ResponseStatusException.class);
    }
}
