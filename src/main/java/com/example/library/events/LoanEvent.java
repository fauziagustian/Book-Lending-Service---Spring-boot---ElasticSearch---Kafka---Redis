package com.example.library.events;

import java.time.Instant;

/**
 * Kafka event emitted by the Book Lending service.
 */
public record LoanEvent(
        String eventId,
        LoanEventType type,
        Long loanId,
        Long bookId,
        Long memberId,
        Instant borrowedAt,
        Instant dueDate,
        Instant returnedAt,
        Instant occurredAt
) { }
