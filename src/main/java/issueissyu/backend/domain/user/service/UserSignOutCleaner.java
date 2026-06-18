package issueissyu.backend.domain.user.service;

import issueissyu.backend.domain.alarm.service.command.PinAlarmCleaner;
import issueissyu.backend.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignOutCleaner {

    private static final String PIN_IDS_OWNED_BY_USER =
            "(SELECT pin_id FROM pin WHERE uid = ?)";

    private static final String ISSUE_PIN_IDS_FOR_OWNED_PINS =
            "(SELECT issue_pin_id FROM issue_pin WHERE pin_id IN "
                    + PIN_IDS_OWNED_BY_USER
                    + ")";

    private static final String EVENT_PIN_IDS_FOR_OWNED_PINS =
            "(SELECT event_pin_id FROM event_pin WHERE pin_id IN " + PIN_IDS_OWNED_BY_USER + ")";

    private final JdbcTemplate jdbcTemplate;
    private final PinAlarmCleaner pinAlarmCleaner;
    private final S3Utils s3Utils;

    @Transactional
    public void deleteRowsReferencingUser(String uid) {
        // DB 삭제 전에 S3 key 일괄 수집
        List<String> s3KeysToDelete = collectS3Keys(uid);

        jdbcTemplate.update(
                "DELETE FROM problem_solver_image WHERE problem_solver_id IN ("
                        + "SELECT problem_solver_id FROM problem_solver WHERE uid = ? "
                        + "OR issue_pin_id IN "
                        + ISSUE_PIN_IDS_FOR_OWNED_PINS
                        + ")",
                uid,
                uid);
        jdbcTemplate.update(
                "DELETE FROM problem_solver WHERE uid = ? OR issue_pin_id IN "
                        + ISSUE_PIN_IDS_FOR_OWNED_PINS,
                uid,
                uid);
        jdbcTemplate.update(
                "DELETE FROM issue_petition WHERE uid = ? OR issue_pin_id IN "
                        + ISSUE_PIN_IDS_FOR_OWNED_PINS,
                uid,
                uid);
        jdbcTemplate.update(
                "DELETE FROM complaint_petition WHERE issue_pin_id IN "
                        + ISSUE_PIN_IDS_FOR_OWNED_PINS,
                uid);
        jdbcTemplate.update(
                "DELETE FROM issue_pin WHERE pin_id IN " + PIN_IDS_OWNED_BY_USER, uid);

        pinAlarmCleaner.deleteByUserOwnedPins(uid);

        jdbcTemplate.update(
                "DELETE FROM cardnews_image_s3 WHERE community_id IN ("
                        + "SELECT community_id FROM community WHERE pin_id IN "
                        + PIN_IDS_OWNED_BY_USER
                        + ")",
                uid);
        jdbcTemplate.update(
                "DELETE FROM community WHERE pin_id IN " + PIN_IDS_OWNED_BY_USER, uid);
        jdbcTemplate.update(
                "DELETE FROM pin_emoji WHERE pin_id IN "
                        + PIN_IDS_OWNED_BY_USER
                        + " OR uid = ?",
                uid,
                uid);
        jdbcTemplate.update(
                "DELETE FROM \"comment\" WHERE pin_id IN "
                        + PIN_IDS_OWNED_BY_USER
                        + " OR uid = ?",
                uid,
                uid);
        jdbcTemplate.update(
                "DELETE FROM declaration WHERE pin_id IN "
                        + PIN_IDS_OWNED_BY_USER
                        + " OR uid = ?",
                uid,
                uid);
        jdbcTemplate.update(
                "DELETE FROM store_image WHERE event_pin_id IN " + EVENT_PIN_IDS_FOR_OWNED_PINS,
                uid);
        jdbcTemplate.update(
                "DELETE FROM event_pin WHERE pin_id IN " + PIN_IDS_OWNED_BY_USER, uid);
        jdbcTemplate.update(
                "DELETE FROM communication_pin WHERE pin_id IN " + PIN_IDS_OWNED_BY_USER,
                uid);
        jdbcTemplate.update(
                "DELETE FROM pin_location WHERE pin_id IN " + PIN_IDS_OWNED_BY_USER, uid);
        jdbcTemplate.update(
                "DELETE FROM pin_image WHERE pin_id IN " + PIN_IDS_OWNED_BY_USER, uid);
        jdbcTemplate.update(
                "DELETE FROM pin_like WHERE pin_id IN " + PIN_IDS_OWNED_BY_USER + " OR uid = ?",
                uid,
                uid);
        jdbcTemplate.update("DELETE FROM notice WHERE pin_id IN " + PIN_IDS_OWNED_BY_USER, uid);
        jdbcTemplate.update("DELETE FROM pin WHERE uid = ?", uid);

        jdbcTemplate.update(
                "DELETE FROM like_alarm WHERE user_alarm_id IN "
                        + "(SELECT user_alarm_id FROM user_alarm WHERE uid = ?)",
                uid);
        jdbcTemplate.update(
                "DELETE FROM hot_alarm WHERE user_alarm_id IN "
                        + "(SELECT user_alarm_id FROM user_alarm WHERE uid = ?)",
                uid);
        jdbcTemplate.update(
                "DELETE FROM event_alarm WHERE user_alarm_id IN "
                        + "(SELECT user_alarm_id FROM user_alarm WHERE uid = ?)",
                uid);
        jdbcTemplate.update(
                "DELETE FROM store_alarm WHERE user_alarm_id IN "
                        + "(SELECT user_alarm_id FROM user_alarm WHERE uid = ?)",
                uid);
        jdbcTemplate.update("DELETE FROM user_alarm WHERE uid = ?", uid);

        jdbcTemplate.update("DELETE FROM user_custom_collection WHERE uid = ?", uid);
        jdbcTemplate.update("DELETE FROM user_emoji WHERE uid = ?", uid);

        // DB 삭제 완료 후 트랜잭션 커밋이 성공하면 S3 객체 일괄 삭제
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            s3KeysToDelete.forEach(s3Utils::deleteIfNotReserved);
                        }
                    });
        } else {
            s3KeysToDelete.forEach(s3Utils::deleteIfNotReserved);
        }
    }

    private List<String> collectS3Keys(String uid) {
        List<String> keys = new ArrayList<>();

        // problem_solver_image: 본인이 참여한 solver + 본인 핀에 달린 solver 이미지
        keys.addAll(jdbcTemplate.queryForList(
                "SELECT problem_solver_image_s3_key FROM problem_solver_image"
                        + " WHERE problem_solver_id IN ("
                        + "SELECT problem_solver_id FROM problem_solver WHERE uid = ?"
                        + " OR issue_pin_id IN " + ISSUE_PIN_IDS_FOR_OWNED_PINS
                        + ")",
                String.class, uid, uid));

        // cardnews_image_s3: 본인 핀의 커뮤니티 카드뉴스 이미지
        keys.addAll(jdbcTemplate.queryForList(
                "SELECT cardnews_image_s3_key FROM cardnews_image_s3"
                        + " WHERE community_id IN ("
                        + "SELECT community_id FROM community WHERE pin_id IN "
                        + PIN_IDS_OWNED_BY_USER + ")",
                String.class, uid));

        // store_image: 본인 핀의 이벤트 핀 스토어 이미지
        keys.addAll(jdbcTemplate.queryForList(
                "SELECT store_image_s3_key FROM store_image"
                        + " WHERE event_pin_id IN " + EVENT_PIN_IDS_FOR_OWNED_PINS,
                String.class, uid));

        // pin_image: 본인 핀의 일반 이미지
        keys.addAll(jdbcTemplate.queryForList(
                "SELECT pin_s3_key FROM pin_image"
                        + " WHERE pin_id IN " + PIN_IDS_OWNED_BY_USER,
                String.class, uid));

        return keys;
    }
}
