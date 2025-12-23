package com.example.library.service;

import com.example.library.domain.Book;
import com.example.library.error.NotFoundException;
import com.example.library.repo.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> list() {
        return bookRepository.findAll();
    }

    public Book get(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new NotFoundException("Book not found: " + id));
    }

    @Transactional
    public Book create(Book book) {
        // availableCopies will be initialized in constructor or validated by caller
        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Long id, String title, String author, String isbn, Integer totalCopies) {
        Book b = get(id);
        if (title != null) b.setTitle(title);
        if (author != null) b.setAuthor(author);
        if (isbn != null) b.setIsbn(isbn);

        if (totalCopies != null) {
            int delta = totalCopies - b.getTotalCopies();
            int newAvailable = b.getAvailableCopies() + delta;
            if (newAvailable < 0) {
                throw new IllegalArgumentException("totalCopies cannot be less than (totalCopies - availableCopies)");
            }
            b.setTotalCopies(totalCopies);
            b.setAvailableCopies(newAvailable);
        }
        return b;
    }

    @Transactional
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new NotFoundException("Book not found: " + id);
        }
        bookRepository.deleteById(id);
    }
}
