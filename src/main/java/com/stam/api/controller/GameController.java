package com.stam.api.controller;

import com.stam.api.dto.GameRequestDTO;
import com.stam.api.entity.Game;
import com.stam.api.repository.GameRepository;
import com.stam.api.service.GameService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/games")
@Tag(name = "Gestion du Catalogue", description = "Endpoints pour consulter et gérer les jeux vidéo")
public class GameController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameService gameService;

    @Operation(summary = "Lister les nouveautés", description = "Renvoie les 10 derniers jeux ajoutés au catalogue.")
    @GetMapping("/latest")
    public ResponseEntity<List<Game>> getLatestGames() {
        return ResponseEntity.ok(gameRepository.findTop10ByOrderByReleaseDateDesc());
    }

    @Operation(summary = "Rechercher des jeux", description = "Permet de lister tous les jeux avec pagination et filtres optionnels (Date, Genre, Prix Max).")
    @GetMapping
    public ResponseEntity<Page<Game>> getGames(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Float maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Game> result;

        if (date != null && genreId != null) {
            result = gameRepository.findByReleaseDateAndGenreId(date, genreId, pageable);
        } else if (date != null) {
            result = gameRepository.findByReleaseDate(date, pageable);
        } else if (genreId != null) {
            result = gameRepository.findByGenreId(genreId, pageable);
        } else if (maxPrice != null) {
            result = gameRepository.findByPriceLessThanEqual(maxPrice, pageable);
        } else {
            result = gameRepository.findAll(pageable);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtenir un jeu par ID", description = "Renvoie les détails d'un jeu spécifique en fonction de son ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable UUID id) {
        Optional<Game> game = gameRepository.findById(id);
        return game.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Ajouter un jeu", description = "Création d'un nouveau jeu par l'administrateur.")
    @PostMapping
    public ResponseEntity<Game> createGame(@Valid @RequestBody GameRequestDTO gameDto) {
        Game savedGame = gameService.createGame(gameDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGame);
    }

    @Operation(summary = "Mettre à jour un jeu", description = "Modification des détails d'un jeu existant par l'administrateur.")
    @PutMapping("/{id}")
    public ResponseEntity<Game> updateGame(@PathVariable UUID id, @Valid @RequestBody GameRequestDTO gameDto) {
        Game updatedGame = gameService.updateGame(id, gameDto);
        return ResponseEntity.ok(updatedGame);
    }

    @Operation(summary = "Supprimer un jeu", description = "Suppression d'un jeu existant par l'administrateur.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable UUID id) {
        if (gameRepository.existsById(id)) {
            gameRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}