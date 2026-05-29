package issueissyu.backend.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

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

    public void deleteRowsReferencingUser(String uid) {
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
    }
}
