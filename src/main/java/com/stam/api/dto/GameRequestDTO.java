package com.stam.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GameRequestDTO {

    @NotBlank(message = "Le titre est obligatoire et ne peut pas être vide.")
    private String title;

    private String description;

    @NotNull(message = "La date de sortie est obligatoire.")
    private LocalDate releaseDate;

    @NotNull(message = "Le prix est obligatoire.")
    @Min(value = 0, message = "Le prix ne peut pas être négatif.")
    private Float price;

    @NotBlank(message = "L'URL de l'image est obligatoire.")
    private String imageUrl;

    @NotNull(message = "L'identifiant du genre est obligatoire.")
    private Long genreId;
}