package com.stam.api.controller;
import com.stam.api.dto.GameRequestDTO;
import com.stam.api.entity.Game;
import com.stam.api.entity.Genre;
import com.stam.api.kafka.CatalogImportProducer;
import com.stam.api.repository.GameRepository;
import com.stam.api.repository.GenreRepository;
import com.stam.api.service.GameService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Gestion du Catalogue", description = "Endpoints pour consulter et gérer les jeux vidéo")
public class GameController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private CatalogImportProducer catalogImportProducer;

    @Operation(summary = "Lister les nouveautés", description = "Renvoie les 10 derniers jeux ajoutés au catalogue.")
    @GetMapping("/games/latest")
    public ResponseEntity<List<Game>> getLatestGames() {
        return ResponseEntity.ok(gameRepository.findTop10ByOrderByReleaseDateDesc());
    }

    @Operation(summary = "Lister les genres", description = "Renvoie la liste des genres disponibles.")
    @GetMapping("/genres")
    public ResponseEntity<List<Genre>> listGenres() {
        return ResponseEntity.ok(genreRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
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

        Sort sort = Sort.by(Sort.Direction.DESC, "releaseDate");
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort);

        List<Game> all = gameRepository.findAll(sort);

        final LocalDate yearStart = year == null ? null : LocalDate.of(year, 1, 1);
        final LocalDate yearEnd = year == null ? null : LocalDate.of(year, 12, 31);
        final String searchLower = (search == null || search.isBlank()) ? null : search.trim().toLowerCase(Locale.ROOT);

        List<Game> filtered = all.stream()
                .filter((g) -> date == null || (g.getReleaseDate() != null && g.getReleaseDate().equals(date)))
                .filter((g) -> yearStart == null || (g.getReleaseDate() != null
                        && !g.getReleaseDate().isBefore(yearStart)
                        && !g.getReleaseDate().isAfter(yearEnd)))
                .filter((g) -> genreId == null || (g.getGenre() != null && Objects.equals(g.getGenre().getId(), genreId)))
                .filter((g) -> maxPrice == null || (g.getPrice() != null && g.getPrice() <= maxPrice))
                .filter((g) -> {
                    if (searchLower == null) return true;
                    String title = g.getTitle() == null ? "" : g.getTitle().toLowerCase(Locale.ROOT);
                    String description = g.getDescription() == null ? "" : g.getDescription().toLowerCase(Locale.ROOT);
                    return title.contains(searchLower) || description.contains(searchLower);
                })
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Game> content = start >= filtered.size() ? List.of() : filtered.subList(start, end);

        return ResponseEntity.ok(new PageImpl<>(content, pageable, filtered.size()));
    }

    @Operation(summary = "Obtenir un jeu par ID", description = "Renvoie les détails d'un jeu spécifique en fonction de son ID.")
    @GetMapping("/games/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable UUID id) {
        Optional<Game> game = gameRepository.findById(id);
        return game.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
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
        if (gameRepository.existsById(id)) {
            gameRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}