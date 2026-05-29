package issueissyu.backend.domain.alarm.repository;

import issueissyu.backend.domain.alarm.entity.UserAlarm;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAlarmListRepository extends JpaRepository<UserAlarm, Long> {

    @Query(
            value =
                    """
                    SELECT ua.user_alarm_id AS alarmId,
                           ua.is_confirmed AS isConfirmed,
                           ua.created_at AS createdAt,
                           CASE
                               WHEN la.like_alarm_id IS NOT NULL THEN 'LIKE'
                               WHEN ea.event_alarm_id IS NOT NULL THEN 'EVENT'
                               WHEN ha.hot_alarm_id IS NOT NULL THEN 'HOT'
                               WHEN sa.store_alarm_id IS NOT NULL THEN 'STORE'
                           END AS alarmType,
                           COALESCE(
                               la.like_alarm_title,
                               ea.event_alarm_title,
                               ha.hot_alarm_title,
                               sa.store_alarm_title
                           ) AS alarmTitle,
                           COALESCE(
                               la.like_alarm_body,
                               ea.event_alarm_body,
                               ha.hot_alarm_body,
                               sa.store_alarm_body
                           ) AS alarmBody,
                           COALESCE(
                               la.like_pin_id,
                               ea.event_pin_id,
                               ha.hot_pin_id,
                               sa.store_pin_id
                           ) AS pinId,
                           COALESCE(
                               la.like_community_id,
                               ea.event_community_id,
                               ha.hot_community_id,
                               sa.store_community_id
                           ) AS communityId
                    FROM user_alarm ua
                    LEFT JOIN like_alarm la ON la.user_alarm_id = ua.user_alarm_id
                    LEFT JOIN event_alarm ea ON ea.user_alarm_id = ua.user_alarm_id
                    LEFT JOIN hot_alarm ha ON ha.user_alarm_id = ua.user_alarm_id
                    LEFT JOIN store_alarm sa ON sa.user_alarm_id = ua.user_alarm_id
                    WHERE ua.uid = :uid
                      AND (
                          :applyCursor = false
                          OR ua.user_alarm_id < :cursorAlarmId
                      )
                    ORDER BY ua.user_alarm_id DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<UserAlarmListRow> findAlarmList(
            @Param("uid") String uid,
            @Param("applyCursor") boolean applyCursor,
            @Param("cursorAlarmId") long cursorAlarmId,
            @Param("limit") int limit);

    @Query(
            value =
                    """
                    SELECT ua.user_alarm_id AS alarmId,
                           ua.is_confirmed AS isConfirmed,
                           ua.created_at AS createdAt,
                           CASE
                               WHEN la.like_alarm_id IS NOT NULL THEN 'LIKE'
                               WHEN ea.event_alarm_id IS NOT NULL THEN 'EVENT'
                               WHEN ha.hot_alarm_id IS NOT NULL THEN 'HOT'
                               WHEN sa.store_alarm_id IS NOT NULL THEN 'STORE'
                           END AS alarmType,
                           COALESCE(
                               la.like_alarm_title,
                               ea.event_alarm_title,
                               ha.hot_alarm_title,
                               sa.store_alarm_title
                           ) AS alarmTitle,
                           COALESCE(
                               la.like_alarm_body,
                               ea.event_alarm_body,
                               ha.hot_alarm_body,
                               sa.store_alarm_body
                           ) AS alarmBody,
                           COALESCE(
                               la.like_pin_id,
                               ea.event_pin_id,
                               ha.hot_pin_id,
                               sa.store_pin_id
                           ) AS pinId,
                           COALESCE(
                               la.like_community_id,
                               ea.event_community_id,
                               ha.hot_community_id,
                               sa.store_community_id
                           ) AS communityId
                    FROM user_alarm ua
                    LEFT JOIN like_alarm la ON la.user_alarm_id = ua.user_alarm_id
                    LEFT JOIN event_alarm ea ON ea.user_alarm_id = ua.user_alarm_id
                    LEFT JOIN hot_alarm ha ON ha.user_alarm_id = ua.user_alarm_id
                    LEFT JOIN store_alarm sa ON sa.user_alarm_id = ua.user_alarm_id
                    WHERE ua.uid = :uid
                      AND ua.user_alarm_id = :alarmId
                    """,
            nativeQuery = true)
    Optional<UserAlarmListRow> findAlarmDetail(
            @Param("uid") String uid, @Param("alarmId") Long alarmId);
}
