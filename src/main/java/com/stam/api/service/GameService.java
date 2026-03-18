package com.stam.api.service;

import com.stam.api.dto.GameRequestDTO;
import com.stam.api.entity.Game;
import com.stam.api.entity.Genre;
import com.stam.api.repository.GameRepository;
import com.stam.api.repository.GenreRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GenreRepository genreRepository;

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

        return gameRepository.save(game);
    }

    public Game updateGame(UUID id, GameRequestDTO dto) {
        Game existingGame = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jeu introuvable."));

        Genre genre = genreRepository.findById(dto.getGenreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le genre spécifié n'existe pas."));

        existingGame.setTitle(dto.getTitle());
        existingGame.setDescription(dto.getDescription());
        existingGame.setReleaseDate(dto.getReleaseDate());
        existingGame.setPrice(dto.getPrice());
        existingGame.setImageUrl(dto.getImageUrl());
        existingGame.setGenre(genre);

        return gameRepository.save(existingGame);
    }
}