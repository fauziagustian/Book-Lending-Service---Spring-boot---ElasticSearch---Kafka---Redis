package com.example.analytics.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LoanEventSearchRepository extends ElasticsearchRepository<LoanEventDocument, String> {
    Page<LoanEventDocument> findByBookId(Long bookId, Pageable pageable);
    Page<LoanEventDocument> findByMemberId(Long memberId, Pageable pageable);
}
