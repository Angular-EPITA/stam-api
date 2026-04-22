package com.stam.api.service;

import com.stam.api.dto.GameRequestDTO;
import com.stam.api.entity.Game;
import com.stam.api.entity.Genre;
import com.stam.api.kafka.GameEventProducer;
import com.stam.api.repository.GameRepository;
import com.stam.api.repository.GameSpecification;
import com.stam.api.repository.GenreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class GameService implements GameServicePort {

    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final GameEventProducer gameEventProducer;

    public GameService(GameRepository gameRepository, GenreRepository genreRepository, GameEventProducer gameEventProducer) {
        this.gameRepository = gameRepository;
        this.genreRepository = genreRepository;
        this.gameEventProducer = gameEventProducer;
    }

    @Override
    public List<Game> getLatestGames() {
        return gameRepository.findTop10ByOrderByReleaseDateDesc();
    }

    @Override
    public List<Genre> listGenres() {
        return genreRepository.findAll();
    }

    @Override
    public Page<Game> getGames(LocalDate date, Integer year, Long genreId, Float maxPrice, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        
        return gameRepository.findAll(GameSpecification.withFilters(date, year, genreId, maxPrice, search), pageable);
    }

    @Override
    public Game getGameById(UUID id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jeu introuvable."));
    }

    @Override
    public Game createGame(GameRequestDTO dto) {
        Genre genre = genreRepository.findById(dto.getGenreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le genre spécifié n'existe pas."));

        Game game = new Game();
        game.setTitle(dto.getTitle());
        game.setDescription(dto.getDescription());
        game.setReleaseDate(dto.getReleaseDate());
        game.setPrice(dto.getPrice());
        game.setImageUrl(dto.getImageUrl());
        game.setGenre(genre);

        Game savedGame = gameRepository.save(game);
        
        gameEventProducer.sendEvent("CREATED", savedGame.getId(), savedGame.getTitle());
        
        return savedGame;
    }

    @Override
    public Game updateGame(UUID id, GameRequestDTO dto) {
        Game existingGame = getGameById(id);

        Genre genre = genreRepository.findById(dto.getGenreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le genre spécifié n'existe pas."));

        existingGame.setTitle(dto.getTitle());
        existingGame.setDescription(dto.getDescription());
        existingGame.setReleaseDate(dto.getReleaseDate());
        existingGame.setPrice(dto.getPrice());
        existingGame.setImageUrl(dto.getImageUrl());
        existingGame.setGenre(genre);

        Game updatedGame = gameRepository.save(existingGame);
        
        gameEventProducer.sendEvent("UPDATED", updatedGame.getId(), updatedGame.getTitle());
        
        return updatedGame;
    }

    @Override
    public boolean deleteGame(UUID id) {
        Game game = getGameById(id);
        
        gameRepository.delete(game);
        
        gameEventProducer.sendEvent("DELETED", id, game.getTitle());
        
        return true;
    }
}