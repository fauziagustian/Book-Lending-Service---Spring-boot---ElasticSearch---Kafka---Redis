package com.example.analytics.events;

import java.time.Instant;

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
