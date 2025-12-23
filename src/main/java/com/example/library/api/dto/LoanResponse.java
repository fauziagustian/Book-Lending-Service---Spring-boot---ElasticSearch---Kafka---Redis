package com.example.library.api.dto;

import com.example.library.domain.Loan;

import java.time.Instant;

public record LoanResponse(
        Long id,
        Long bookId,
        Long memberId,
        Instant borrowedAt,
        Instant dueDate,
        Instant returnedAt
) {
    public static LoanResponse from(Loan l) {
        return new LoanResponse(
                l.getId(),
                l.getBook().getId(),
                l.getMember().getId(),
                l.getBorrowedAt(),
                l.getDueDate(),
                l.getReturnedAt()
        );
    }
}
