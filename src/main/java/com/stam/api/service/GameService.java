package com.stam.api.service;

import com.stam.api.dto.GameRequestDTO;
import com.stam.api.entity.Game;
import com.stam.api.entity.Genre;
import com.stam.api.kafka.GameEventProducer;
import com.stam.api.repository.GameRepository;
import com.stam.api.repository.GameSpecification;
import com.stam.api.repository.GenreRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameService implements GameServicePort {

    private final GameRepository gameRepository;

    private final GenreRepository genreRepository;

    private final GameEventProducer gameEventProducer;

    public List<Game> getLatestGames() {
        return gameRepository.findTop10ByOrderByReleaseDateDesc();
    }

    public List<Genre> listGenres() {
        return genreRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Page<Game> getGames(LocalDate date, Integer year, Long genreId, Float maxPrice, String search, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "releaseDate");
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort);
        return gameRepository.findAll(
                GameSpecification.withFilters(date, year, genreId, maxPrice, search),
                pageable
        );
    }

    public Game getGameById(UUID id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jeu introuvable."));
    }

    public Game createGame(GameRequestDTO dto) {
        Genre genre = findGenreOrThrow(dto.getGenreId());
        Game game = new Game();
        mapDtoToEntity(dto, game, genre);
        Game saved = gameRepository.save(game);
        gameEventProducer.publish("CREATED", saved.getId(), saved.getTitle());
        return saved;
    }

    public Game updateGame(UUID id, GameRequestDTO dto) {
        Game existingGame = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jeu introuvable."));
        Genre genre = findGenreOrThrow(dto.getGenreId());
        mapDtoToEntity(dto, existingGame, genre);
        Game saved = gameRepository.save(existingGame);
        gameEventProducer.publish("UPDATED", saved.getId(), saved.getTitle());
        return saved;
    }

    public boolean deleteGame(UUID id) {
        return gameRepository.findById(id)
                .map(game -> {
                    gameRepository.deleteById(id);
                    gameEventProducer.publish("DELETED", game.getId(), game.getTitle());
                    return true;
                })
                .orElse(false);
    }

    private Genre findGenreOrThrow(Long genreId) {
        return genreRepository.findById(genreId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le genre spécifié n'existe pas."));
    }

    private void mapDtoToEntity(GameRequestDTO dto, Game game, Genre genre) {
        game.setTitle(dto.getTitle());
        game.setDescription(dto.getDescription());
        game.setReleaseDate(dto.getReleaseDate());
        game.setPrice(dto.getPrice());
        game.setImageUrl(dto.getImageUrl());
        game.setGenre(genre);
    }
}