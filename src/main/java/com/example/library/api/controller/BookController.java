package com.example.library.api.controller;

import com.example.library.api.dto.BaseResponse;
import com.example.library.api.dto.BookRequest;
import com.example.library.api.dto.BookResponse;
import com.example.library.domain.Book;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    private <T> BaseResponse<T> ok(T data) {
        BaseResponse<T> res = BaseResponse.<T>builder()
                .responseData(data)
                .build();
        res.setResponseSucceed();
        return res;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public BaseResponse<List<BookResponse>> list() {
        List<BookResponse> books = bookService.list().stream().map(BookResponse::from).toList();
        return ok(books);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public BaseResponse<BookResponse> get(@PathVariable Long id) {
        return ok(BookResponse.from(bookService.get(id)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<BookResponse> create(@Valid @RequestBody BookRequest req) {
        Book book = new Book(req.title(), req.author(), req.isbn(), req.totalCopies());
        return ok(BookResponse.from(bookService.create(book)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<BookResponse> update(@PathVariable Long id, @Valid @RequestBody BookRequest req) {
        Book updated = bookService.update(id, req.title(), req.author(), req.isbn(), req.totalCopies());
        return ok(BookResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ok(null);
    }
}
