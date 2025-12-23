package com.example.library.api.controller;

import com.example.library.api.dto.BaseResponse;
import com.example.library.api.dto.MemberRequest;
import com.example.library.api.dto.MemberResponse;
import com.example.library.domain.Member;
import com.example.library.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    private <T> BaseResponse<T> ok(T data) {
        BaseResponse<T> res = BaseResponse.<T>builder()
                .responseData(data)
                .build();
        res.setResponseSucceed();
        return res;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<List<MemberResponse>> list() {
        List<MemberResponse> members = memberService.list().stream().map(MemberResponse::from).toList();
        return ok(members);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public BaseResponse<MemberResponse> get(@PathVariable Long id) {
        return ok(MemberResponse.from(memberService.get(id)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<MemberResponse> create(@Valid @RequestBody MemberRequest req) {
        Member member = new Member(req.name(), req.email());
        return ok(MemberResponse.from(memberService.create(member)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<MemberResponse> update(@PathVariable Long id, @Valid @RequestBody MemberRequest req) {
        Member updated = memberService.update(id, req.name(), req.email());
        return ok(MemberResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ok(null);
    }
}
