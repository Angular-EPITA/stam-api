package com.stam.api.repository;

import com.stam.api.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID>, JpaSpecificationExecutor<Game> {
    
    List<Game> findTop10ByOrderByReleaseDateDesc();
}