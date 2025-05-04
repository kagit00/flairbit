package com.dating.flairbit.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReelInteractionViewRefresher {

    private final JdbcTemplate jdbcTemplate;

//    @Scheduled(fixedRate = 10 * 60 * 1000, initialDelay = 60 * 1000)
    @Transactional
    public void refreshMaterializedViews() {
        try {
            jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY media_like_counts");
            jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY media_view_counts");
            log.info("Refreshed materialized views: media_like_counts, media_view_counts");

        } catch (Exception ex) {
            log.error("Failed to refresh materialized views", ex);
            throw ex;
        }
    }
}
