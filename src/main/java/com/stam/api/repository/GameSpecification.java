package com.stam.api.repository;

import com.stam.api.entity.Game;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class GameSpecification {

    private GameSpecification() {}

    public static Specification<Game> withFilters(LocalDate date, Integer year, Long genreId, Float maxPrice, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (date != null) {
                predicates.add(cb.equal(root.get("releaseDate"), date));
            }

            if (year != null) {
                LocalDate yearStart = LocalDate.of(year, 1, 1);
                LocalDate yearEnd = LocalDate.of(year, 12, 31);
                predicates.add(cb.between(root.get("releaseDate"), yearStart, yearEnd));
            }

            if (genreId != null) {
                predicates.add(cb.equal(root.get("genre").get("id"), genreId));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
