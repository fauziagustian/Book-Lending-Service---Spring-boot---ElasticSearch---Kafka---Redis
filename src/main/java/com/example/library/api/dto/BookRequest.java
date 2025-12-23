package com.example.library.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BookRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank String isbn,
        @Min(0) int totalCopies
) {}
