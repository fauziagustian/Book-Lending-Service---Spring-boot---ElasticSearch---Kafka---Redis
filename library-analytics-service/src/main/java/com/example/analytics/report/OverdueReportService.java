package com.example.analytics.report;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class OverdueReportService {

    private final JdbcTemplate jdbcTemplate;

    public OverdueReportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Advanced native SQL example:
     * - aggregates overdue counts per member
     * - uses a window function (DENSE_RANK) to rank members by overdue_count
     */
    public List<OverdueMemberRow> topOverdueMembers(Instant now, int limit) {
        String sql = """
                SELECT member_id, name, email, overdue_count, rnk
                FROM (
                    SELECT
                        m.id AS member_id,
                        m.name AS name,
                        m.email AS email,
                        COUNT(l.id) AS overdue_count,
                        DENSE_RANK() OVER (ORDER BY COUNT(l.id) DESC) AS rnk
                    FROM members m
                    JOIN loans l ON l.member_id = m.id
                    WHERE l.returned_at IS NULL
                      AND l.due_date < ?
                    GROUP BY m.id, m.name, m.email
                ) t
                ORDER BY overdue_count DESC, member_id ASC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> OverdueMemberRow.builder()
                .memberId(rs.getLong("member_id"))
                .name(rs.getString("name"))
                .email(rs.getString("email"))
                .overdueCount(rs.getLong("overdue_count"))
                .rank(rs.getInt("rnk"))
                .build(), Timestamp.from(now), limit);
    }
}
