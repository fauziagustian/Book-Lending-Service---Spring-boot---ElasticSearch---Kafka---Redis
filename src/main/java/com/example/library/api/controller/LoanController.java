package com.example.library.api.controller;

import com.example.library.api.dto.BaseResponse;
import com.example.library.api.dto.BorrowRequest;
import com.example.library.api.dto.LoanResponse;
import com.example.library.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    private <T> BaseResponse<T> ok(T data) {
        BaseResponse<T> res = BaseResponse.<T>builder()
                .responseData(data)
                .build();
        res.setResponseSucceed();
        return res;
    }

    @PostMapping("/borrow")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public BaseResponse<LoanResponse> borrow(@Valid @RequestBody BorrowRequest req) {
        return ok(LoanResponse.from(loanService.borrow(req.bookId(), req.memberId())));
    }

    @PostMapping("/{loanId}/return")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public BaseResponse<LoanResponse> returnLoan(@PathVariable Long loanId) {
        return ok(LoanResponse.from(loanService.returnLoan(loanId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public BaseResponse<List<LoanResponse>> listByMember(@RequestParam Long memberId) {
        List<LoanResponse> loans = loanService.listLoansByMember(memberId).stream().map(LoanResponse::from).toList();
        return ok(loans);
    }
}
