package com.example.analytics.search;

import com.example.analytics.events.LoanEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "loan-events")
public class LoanEventDocument {
    @Id
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
