package com.example.library.events;

import com.example.library.domain.Loan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Publishes loan lifecycle events to Kafka. Publishing is best-effort:
 * failures won't block the main borrow/return flow.
 */
@Component
public class LoanEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoanEventPublisher.class);

    private final KafkaTemplate<String, LoanEvent> kafkaTemplate;
    private final boolean enabled;
    private final String topic;

    public LoanEventPublisher(KafkaTemplate<String, LoanEvent> kafkaTemplate,
                              @Value("${library.events.loan.enabled:true}") boolean enabled,
                              @Value("${library.events.loan.topic:library.loan-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.enabled = enabled;
        this.topic = topic;
    }

    public void publishBorrowed(Loan loan) {
        publish(LoanEventType.BORROWED, loan);
    }

    public void publishReturned(Loan loan) {
        publish(LoanEventType.RETURNED, loan);
    }

    private void publish(LoanEventType type, Loan loan) {
        if (!enabled) return;

        try {
            Instant now = Instant.now();
            LoanEvent evt = new LoanEvent(
                    UUID.randomUUID().toString(),
                    type,
                    loan.getId(),
                    loan.getBook().getId(),
                    loan.getMember().getId(),
                    loan.getBorrowedAt(),
                    loan.getDueDate(),
                    loan.getReturnedAt(),
                    now
            );
            kafkaTemplate.send(topic, String.valueOf(loan.getId()), evt);
        } catch (Exception e) {
            log.warn("Failed to publish loan event type={} loanId={}: {}", type, loan.getId(), e.getMessage());
        }
    }
}
