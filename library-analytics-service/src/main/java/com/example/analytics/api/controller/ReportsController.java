package com.example.analytics.api.controller;

import com.example.analytics.api.dto.BaseResponse;
import com.example.analytics.report.OverdueMemberRow;
import com.example.analytics.report.OverdueReportService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(path = "/api/reports", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReportsController {

    private final OverdueReportService reportService;

    public ReportsController(OverdueReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/overdue-members")
    public BaseResponse<List<OverdueMemberRow>> overdueMembers(@RequestParam(defaultValue = "10") int limit) {
        List<OverdueMemberRow> data = reportService.topOverdueMembers(Instant.now(), limit);
        BaseResponse<List<OverdueMemberRow>> res = new BaseResponse<>();
        res.setResponseSucceed();
        res.setResponseData(data);
        return res;
    }
}
