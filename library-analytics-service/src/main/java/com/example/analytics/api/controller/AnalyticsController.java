package com.example.analytics.api.controller;

import com.example.analytics.api.dto.BaseResponse;
import com.example.analytics.api.dto.TopBookDto;
import com.example.analytics.redis.TopBooksCacheService;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/analytics", produces = MediaType.APPLICATION_JSON_VALUE)
public class AnalyticsController {

    private final TopBooksCacheService cacheService;

    public AnalyticsController(TopBooksCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/top-books")
    public BaseResponse<List<TopBookDto>> topBooks(@RequestParam(defaultValue = "10") int limit) {
        Set<ZSetOperations.TypedTuple<String>> tuples = cacheService.getTopBooks(limit);
        List<TopBookDto> data = tuples == null ? Collections.emptyList() :
                tuples.stream()
                        .map(t -> TopBookDto.builder()
                                .bookId(Long.valueOf(t.getValue()))
                                .borrowCount(t.getScore())
                                .build())
                        .collect(Collectors.toList());

        BaseResponse<List<TopBookDto>> res = new BaseResponse<>();
        res.setResponseSucceed();
        res.setResponseData(data);
        return res;
    }
}
