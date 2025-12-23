package com.example.library.api.dto;

import com.example.library.domain.Book;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        int totalCopies,
        int availableCopies
) {
    public static BookResponse from(Book b) {
        return new BookResponse(b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getTotalCopies(), b.getAvailableCopies());
    }
}
