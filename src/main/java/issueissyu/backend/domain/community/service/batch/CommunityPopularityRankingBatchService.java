package issueissyu.backend.domain.community.service.batch;

import issueissyu.backend.global.config.properties.CommunityPopularityProperties;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityPopularityRankingBatchService {

    private static final RowMapper<SeedRow> ROW_MAPPER = new SeedRowMapper();

    /**
     * 피드 후보 타입(ISSUE·STORE·COMMUNICATION) 커뮤니티와 핀 지표·댓글 수만 배치에서 집계합니다.
     */
    private static final String SEED_SQL =
            """
            WITH eligible AS (
                SELECT DISTINCT c.community_id, c.pin_id
                FROM community c
                WHERE c.community_type IN ('ISSUE', 'STORE', 'COMMUNICATION')
            )
            SELECT e.community_id,
                   p.view_count,
                   p.like_count,
                   COALESCE(
                       (SELECT COUNT(*) FROM "comment" cm WHERE cm.pin_id = e.pin_id),
                       0) AS comment_cnt
            FROM eligible e
            JOIN pin p ON p.pin_id = e.pin_id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final CommunityPopularityProperties properties;

    /**
     * 인기도 = (조회수 × wV) + (댓글 수 × wC) + (좋아요 수 × wL) − chartRankSubtract<br>
     * 외부 차트 순위는 미연동 시 {@code chartRankSubtract=0}.
     */
    @Transactional
    public void refreshAllCommunityPopularity() {
        jdbcTemplate.update(
                "UPDATE community SET popularity = NULL WHERE community_type IN ('ISSUE', 'STORE', 'COMMUNICATION')");

        List<SeedRow> rows = jdbcTemplate.query(SEED_SQL, ROW_MAPPER);
        double wv = properties.getWeightView();
        double wc = properties.getWeightComment();
        double wl = properties.getWeightLike();
        double penalty = properties.getChartRankSubtract();

        int updated = 0;
        for (SeedRow row : rows) {
            double score =
                    popularity(row.viewCount(), row.commentCount(), row.likeCount(), penalty, wv, wc, wl);
            jdbcTemplate.update(
                    "UPDATE community SET popularity = ? WHERE community_id = ?", score, row.communityId());
            updated++;
        }
        log.info("커뮤니티 인기도 갱신 완료: rows={}", updated);
    }

    static double popularity(
            int viewCount,
            long commentCount,
            int likeCount,
            double chartRankSubtract,
            double weightView,
            double weightComment,
            double weightLike) {
        return viewCount * weightView + commentCount * weightComment + likeCount * weightLike - chartRankSubtract;
    }

    private record SeedRow(long communityId, int viewCount, int likeCount, long commentCount) {}

    private static final class SeedRowMapper implements RowMapper<SeedRow> {
        @Override
        public SeedRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new SeedRow(
                    rs.getLong("community_id"),
                    rs.getInt("view_count"),
                    rs.getInt("like_count"),
                    rs.getLong("comment_cnt"));
        }
    }
}
