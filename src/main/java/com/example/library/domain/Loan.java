package com.example.library.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "loans", indexes = {
        @Index(name = "idx_loans_member_active", columnList = "member_id, returned_at"),
        @Index(name = "idx_loans_due_active", columnList = "member_id, due_date, returned_at")
})
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_loans_book"))
    private Book book;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_loans_member"))
    private Member member;

    @Column(name = "borrowed_at", nullable = false)
    private Instant borrowedAt;

    @Column(name = "due_date", nullable = false)
    private Instant dueDate;

    @Column(name = "returned_at")
    private Instant returnedAt;

    protected Loan() {}

    public Loan(Book book, Member member, Instant borrowedAt, Instant dueDate) {
        this.book = book;
        this.member = member;
        this.borrowedAt = borrowedAt;
        this.dueDate = dueDate;
    }

    public Long getId() { return id; }
    public Book getBook() { return book; }
    public Member getMember() { return member; }
    public Instant getBorrowedAt() { return borrowedAt; }
    public Instant getDueDate() { return dueDate; }
    public Instant getReturnedAt() { return returnedAt; }

    public boolean isActive() {
        return returnedAt == null;
    }

    public boolean isOverdue(Instant now) {
        return isActive() && dueDate.isBefore(now);
    }

    public void markReturned(Instant returnedAt) {
        if (this.returnedAt != null) {
            throw new IllegalStateException("Loan already returned");
        }
        this.returnedAt = returnedAt;
    }
}
