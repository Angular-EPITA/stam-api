package com.stam.api.service;

import com.stam.api.dto.GameRequestDTO;
import com.stam.api.entity.Game;
import com.stam.api.entity.Genre;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Port primaire (hexagonal) — contrat du service de gestion du catalogue.
 * Les adaptateurs d'entrée (contrôleurs REST, consumers Kafka) dépendent
 * de cette interface, jamais de l'implémentation concrète.
 */
public interface GameServicePort {

    List<Game> getLatestGames();

    List<Genre> listGenres();

    Page<Game> getGames(LocalDate date, Integer year, Long genreId, Float maxPrice, String search, int page, int size);

    Game getGameById(UUID id);

    Game createGame(GameRequestDTO dto);

    Game updateGame(UUID id, GameRequestDTO dto);

    boolean deleteGame(UUID id);
}
