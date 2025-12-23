package com.example.analytics.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueMemberRow {
    private Long memberId;
    private String name;
    private String email;
    private Long overdueCount;
    private Integer rank;
}
