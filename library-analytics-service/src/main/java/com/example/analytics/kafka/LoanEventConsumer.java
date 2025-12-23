package com.example.analytics.kafka;

import com.example.analytics.events.LoanEvent;
import com.example.analytics.events.LoanEventType;
import com.example.analytics.redis.TopBooksCacheService;
import com.example.analytics.search.LoanEventDocument;
import com.example.analytics.search.LoanEventSearchRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LoanEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LoanEventConsumer.class);

    private final TopBooksCacheService cacheService;
    private final LoanEventSearchRepository searchRepository;
    private final ObjectMapper objectMapper;

    public LoanEventConsumer(TopBooksCacheService cacheService,
                             LoanEventSearchRepository searchRepository,
                             ObjectMapper objectMapper) {
        this.cacheService = cacheService;
        this.searchRepository = searchRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "${library.events.loan.topic:library.loan-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received message from topic={}, partition={}, offset={}", topic, partition, offset);
            log.debug("Raw message: {}", message);

            // Parse JSON manually to avoid deserialization issues
            LoanEvent event = parseMessage(message);
            
            log.info("Consumed loan event type={} loanId={} bookId={} memberId={}",
                    event.type(), event.loanId(), event.bookId(), event.memberId());

            // Process: Update Redis cache
            if (event.type() == LoanEventType.BORROWED) {
                cacheService.incrementBorrowCount(event.bookId());
                log.info("Incremented borrow count for bookId={}", event.bookId());
            }

            // Process: Save to Elasticsearch
            LoanEventDocument doc = LoanEventDocument.builder()
                    .eventId(event.eventId())
                    .type(event.type())
                    .loanId(event.loanId())
                    .bookId(event.bookId())
                    .memberId(event.memberId())
                    .borrowedAt(event.borrowedAt())
                    .dueDate(event.dueDate())
                    .returnedAt(event.returnedAt())
                    .occurredAt(event.occurredAt())
                    .build();

            searchRepository.save(doc);
            log.info("Saved loan event to Elasticsearch: eventId={}", event.eventId());

            // Acknowledge successful processing
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            
            log.info("Successfully processed message at offset={}", offset);

        } catch (Exception e) {
            log.error("Failed to process message at offset={}: {}", offset, e.getMessage(), e);
            
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            
        }
    }

    private LoanEvent parseMessage(String message) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(message);
        
        // Extract fields from JSON
        String eventId = jsonNode.has("eventId") ? jsonNode.get("eventId").asText() : null;
        String typeStr = jsonNode.has("type") ? jsonNode.get("type").asText() : null;
        Long loanId = jsonNode.has("loanId") ? jsonNode.get("loanId").asLong() : null;
        Long bookId = jsonNode.has("bookId") ? jsonNode.get("bookId").asLong() : null;
        Long memberId = jsonNode.has("memberId") ? jsonNode.get("memberId").asLong() : null;
        
        Instant borrowedAt = parseInstant(jsonNode, "borrowedAt");
        Instant dueDate = parseInstant(jsonNode, "dueDate");
        Instant returnedAt = parseInstant(jsonNode, "returnedAt");
        Instant occurredAt = parseInstant(jsonNode, "occurredAt");
        
        LoanEventType type = parseEventType(typeStr);
        
        return new LoanEvent(eventId, type, loanId, bookId, memberId, 
                           borrowedAt, dueDate, returnedAt, occurredAt);
    }

    private Instant parseInstant(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) {
            return null;
        }

        String value = node.get(field).asText();
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            log.error("Invalid Instant format field={} value={}", field, value);
            return null;
        }
    }


    private LoanEventType parseEventType(String typeStr) {
    if (typeStr == null) {
        throw new IllegalArgumentException("Event type is null");
    }

    try {
        return LoanEventType.valueOf(typeStr.toUpperCase());
    } catch (IllegalArgumentException e) {
        log.error("Unknown LoanEventType: {}", typeStr);
        throw e;
    }
}

}