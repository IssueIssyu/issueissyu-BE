package issueissyu.backend.domain.alarm.service.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PinAlarmCleaner {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void deleteByPinId(Long pinId, Long communityId) {
        deleteSubAlarms("like_alarm", "like_pin_id", "like_community_id", pinId, communityId);
        deleteSubAlarms("event_alarm", "event_pin_id", "event_community_id", pinId, communityId);
        deleteSubAlarms("hot_alarm", "hot_pin_id", "hot_community_id", pinId, communityId);
        deleteSubAlarms("store_alarm", "store_pin_id", "store_community_id", pinId, communityId);
    }

    private void deleteSubAlarms(
            String tableName,
            String pinColumn,
            String communityColumn,
            Long pinId,
            Long communityId) {
        String whereClause = buildWhereClause(pinColumn, communityColumn, communityId != null);
        Object[] params = buildParams(pinId, communityId, communityId != null);

        List<Long> userAlarmIds =
                jdbcTemplate.queryForList(
                        "SELECT user_alarm_id FROM " + tableName + " WHERE " + whereClause,
                        Long.class,
                        params);
        if (userAlarmIds.isEmpty()) {
            return;
        }

        jdbcTemplate.update("DELETE FROM " + tableName + " WHERE " + whereClause, params);
        deleteUserAlarms(userAlarmIds);
    }

    private static String buildWhereClause(String pinColumn, String communityColumn, boolean includeCommunity) {
        if (includeCommunity) {
            return pinColumn + " = ? OR " + communityColumn + " = ?";
        }
        return pinColumn + " = ?";
    }

    private static Object[] buildParams(Long pinId, Long communityId, boolean includeCommunity) {
        if (includeCommunity) {
            return new Object[] {pinId, communityId};
        }
        return new Object[] {pinId};
    }

    private void deleteUserAlarms(List<Long> userAlarmIds) {
        String placeholders = String.join(",", userAlarmIds.stream().map(id -> "?").toList());
        Object[] params = userAlarmIds.toArray();
        jdbcTemplate.update(
                "DELETE FROM user_alarm WHERE user_alarm_id IN (" + placeholders + ")", params);
    }
}
