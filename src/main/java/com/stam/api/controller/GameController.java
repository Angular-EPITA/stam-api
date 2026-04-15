package com.stam.api.controller;
import com.stam.api.dto.GameRequestDTO;
import com.stam.api.entity.Game;
import com.stam.api.entity.Genre;
import com.stam.api.service.GameServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Gestion du Catalogue", description = "Endpoints pour consulter et gérer les jeux vidéo")
public class GameController {

    private final GameServicePort gameService;

    @Operation(summary = "Lister les nouveautés", description = "Renvoie les 10 derniers jeux ajoutés au catalogue.")
    @GetMapping("/games/latest")
    public ResponseEntity<List<Game>> getLatestGames() {
        return ResponseEntity.ok(gameService.getLatestGames());
    }

    @Operation(summary = "Lister les genres", description = "Renvoie la liste des genres disponibles.")
    @GetMapping("/genres")
    public ResponseEntity<List<Genre>> listGenres() {
        return ResponseEntity.ok(gameService.listGenres());
    }

    @Operation(summary = "Rechercher des jeux", description = "Permet de lister tous les jeux avec pagination et filtres optionnels (Date, Genre, Prix Max).")
    @GetMapping("/games")
    public ResponseEntity<Page<Game>> getGames(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Float maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(gameService.getGames(date, year, genreId, maxPrice, search, page, size));
    }

    @Operation(summary = "Obtenir un jeu par ID", description = "Renvoie les détails d'un jeu spécifique en fonction de son ID.")
    @GetMapping("/games/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable UUID id) {
        return ResponseEntity.ok(gameService.getGameById(id));
    }

    @Operation(summary = "Ajouter un jeu", description = "Création d'un nouveau jeu par l'administrateur.")
    @PostMapping("/games")
    public ResponseEntity<Game> createGame(@Valid @RequestBody GameRequestDTO gameDto) {
        Game savedGame = gameService.createGame(gameDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGame);
    }

    @Operation(summary = "Mettre à jour un jeu", description = "Modification des détails d'un jeu existant par l'administrateur.")
    @PutMapping("/games/{id}")
    public ResponseEntity<Game> updateGame(@PathVariable UUID id, @Valid @RequestBody GameRequestDTO gameDto) {
        Game updatedGame = gameService.updateGame(id, gameDto);
        return ResponseEntity.ok(updatedGame);
    }

    @Operation(summary = "Supprimer un jeu", description = "Suppression d'un jeu existant par l'administrateur.")
    @DeleteMapping("/games/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable UUID id) {
        if (gameService.deleteGame(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}