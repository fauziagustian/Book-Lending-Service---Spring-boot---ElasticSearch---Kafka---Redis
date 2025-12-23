package com.example.library.service;

import com.example.library.domain.Member;
import com.example.library.error.NotFoundException;
import com.example.library.repo.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> list() {
        return memberRepository.findAll();
    }

    public Member get(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new NotFoundException("Member not found: " + id));
    }

    @Transactional
    public Member create(Member member) {
        return memberRepository.save(member);
    }

    @Transactional
    public Member update(Long id, String name, String email) {
        Member m = get(id);
        if (name != null) m.setName(name);
        if (email != null) m.setEmail(email);
        return m;
    }

    @Transactional
    public void delete(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new NotFoundException("Member not found: " + id);
        }
        memberRepository.deleteById(id);
    }
}
