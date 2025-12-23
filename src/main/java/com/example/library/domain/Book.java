package com.example.library.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "books", uniqueConstraints = {
        @UniqueConstraint(name = "uk_books_isbn", columnNames = {"isbn"})
})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String author;

    @NotBlank
    @Column(nullable = false, length = 32)
    private String isbn;

    @Min(0)
    @Column(nullable = false)
    private int totalCopies;

    @Min(0)
    @Column(nullable = false)
    private int availableCopies;

    @Version
    private long version;

    protected Book() {}

    public Book(String title, String author, String isbn, int totalCopies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public void borrowOne() {
        if (availableCopies <= 0) {
            throw new IllegalStateException("No available copies");
        }
        this.availableCopies -= 1;
    }

    public void returnOne() {
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException("Available copies cannot exceed total copies");
        }
        this.availableCopies += 1;
    }
}
