package com.example.library.service;

import com.example.library.config.LibraryRulesProperties;
import com.example.library.domain.Book;
import com.example.library.domain.Loan;
import com.example.library.domain.Member;
import com.example.library.error.BusinessRuleViolationException;
import com.example.library.error.ConflictException;
import com.example.library.error.NotFoundException;
import com.example.library.events.LoanEventPublisher;
import com.example.library.repo.BookRepository;
import com.example.library.repo.LoanRepository;
import com.example.library.repo.MemberRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LoanService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final LibraryRulesProperties rules;
    private final Clock clock;


    private final LoanEventPublisher loanEventPublisher;

    private final Counter loansBorrowed;
    private final Counter loansReturned;

    public LoanService(BookRepository bookRepository,
                       MemberRepository memberRepository,
                       LoanRepository loanRepository,
                       LibraryRulesProperties rules,
                       Clock clock,
                       MeterRegistry meterRegistry, LoanEventPublisher loanEventPublisher) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.rules = rules;
        this.clock = clock;
        this.loanEventPublisher = loanEventPublisher;
        this.loansBorrowed = meterRegistry.counter("library_loans_borrowed_total");
        this.loansReturned = meterRegistry.counter("library_loans_returned_total");
    }

    public List<Loan> listLoansByMember(Long memberId) {
        return loanRepository.findByMemberId(memberId);
    }

    @Transactional
    public Loan borrow(Long bookId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found: " + memberId));

        Instant now = Instant.now(clock);

        // Rule: no borrowing with overdue active loans
        if (loanRepository.existsByMemberAndReturnedAtIsNullAndDueDateBefore(member, now)) {
            throw new BusinessRuleViolationException("Member has overdue loans and cannot borrow new books");
        }

        // Rule: maximum active loans
        long active = loanRepository.countByMemberAndReturnedAtIsNull(member);
        if (active >= rules.getMaxActiveLoans()) {
            throw new BusinessRuleViolationException("Member reached max active loans: " + rules.getMaxActiveLoans());
        }

        // Lock the book row to avoid races on availableCopies
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found: " + bookId));

        if (book.getAvailableCopies() <= 0) {
            throw new ConflictException("No available copies for bookId=" + bookId);
        }

        book.borrowOne();

        Instant due = now.plus(rules.getLoanDurationDays(), ChronoUnit.DAYS);
        Loan loan = new Loan(book, member, now, due);
        Loan saved = loanRepository.save(loan);
        loansBorrowed.increment();
        loanEventPublisher.publishBorrowed(saved);
        return saved;
    }

    @Transactional
    public Loan returnLoan(Long loanId) {
        Loan loan = loanRepository.findByIdAndReturnedAtIsNull(loanId)
                .orElseThrow(() -> new NotFoundException("Active loan not found: " + loanId));

        Instant now = Instant.now(clock);

        // Lock the book row to safely increment available copies
        Long bookId = loan.getBook().getId();
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found: " + bookId));

        loan.markReturned(now);
        book.returnOne();

        loansReturned.increment();
        loanEventPublisher.publishReturned(loan);
        return loan;
    }
}
