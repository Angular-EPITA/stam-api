package com.stam.api.repository;

import com.stam.api.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
    
    List<Game> findTop10ByOrderByReleaseDateDesc();

    Page<Game> findByReleaseDate(LocalDate releaseDate, Pageable pageable);
    
    Page<Game> findByGenreId(Long genreId, Pageable pageable);
    
    Page<Game> findByPriceLessThanEqual(Float price, Pageable pageable);
    
    Page<Game> findByReleaseDateAndGenreId(LocalDate releaseDate, Long genreId, Pageable pageable);
}