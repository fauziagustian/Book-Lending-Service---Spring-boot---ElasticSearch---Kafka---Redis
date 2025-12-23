package com.example.analytics.api.dto;

import com.example.analytics.events.LoanEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanEventSearchResponse {
    private String eventId;
    private LoanEventType type;
    private Long loanId;
    private Long bookId;
    private Long memberId;
    private Instant borrowedAt;
    private Instant dueDate;
    private Instant returnedAt;
    private Instant occurredAt;
}
