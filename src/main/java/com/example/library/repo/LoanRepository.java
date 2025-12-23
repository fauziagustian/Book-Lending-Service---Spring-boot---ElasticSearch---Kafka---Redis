package com.example.library.repo;

import com.example.library.domain.Loan;
import com.example.library.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    long countByMemberAndReturnedAtIsNull(Member member);

    boolean existsByMemberAndReturnedAtIsNullAndDueDateBefore(Member member, Instant now);

    List<Loan> findByMemberId(Long memberId);

    Optional<Loan> findByIdAndReturnedAtIsNull(Long id);
}
