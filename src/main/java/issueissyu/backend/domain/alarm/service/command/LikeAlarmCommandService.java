package issueissyu.backend.domain.alarm.service.command;

import java.util.Optional;

public interface LikeAlarmCommandService {

    /** POST /api/alarms/like/{pinId} — like_alarm_active 가 false 이면 403 */
    LikeAlarmPrepared createLikeAlarmForApi(String likerUid, Long pinId);

    /** POST /api/pins/{pinId}/like — 비활성·자기 핀 등은 조용히 스킵 */
    Optional<LikeAlarmPrepared> createLikeAlarmIfEligible(String likerUid, Long pinId);
}
