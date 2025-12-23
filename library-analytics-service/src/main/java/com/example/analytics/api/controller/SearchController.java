package com.example.analytics.api.controller;

import com.example.analytics.api.dto.BaseResponse;
import com.example.analytics.api.dto.LoanEventSearchResponse;
import com.example.analytics.search.LoanEventDocument;
import com.example.analytics.search.LoanEventSearchRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/search", produces = MediaType.APPLICATION_JSON_VALUE)
public class SearchController {

    private final LoanEventSearchRepository repository;

    public SearchController(LoanEventSearchRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/loan-events")
    public BaseResponse<List<LoanEventSearchResponse>> loanEvents(
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        List<LoanEventDocument> docs;
        if (bookId != null) {
            docs = repository.findByBookId(bookId, pageable).getContent();
        } else if (memberId != null) {
            docs = repository.findByMemberId(memberId, pageable).getContent();
        } else {
            docs = repository.findAll(pageable).getContent();
        }

        List<LoanEventSearchResponse> data = docs.stream()
                .map(d -> LoanEventSearchResponse.builder()
                        .eventId(d.getEventId())
                        .type(d.getType())
                        .loanId(d.getLoanId())
                        .bookId(d.getBookId())
                        .memberId(d.getMemberId())
                        .borrowedAt(d.getBorrowedAt())
                        .dueDate(d.getDueDate())
                        .returnedAt(d.getReturnedAt())
                        .occurredAt(d.getOccurredAt())
                        .build())
                .toList();

        BaseResponse<List<LoanEventSearchResponse>> res = new BaseResponse<>();
        res.setResponseSucceed();
        res.setResponseData(data);
        return res;
    }
}
