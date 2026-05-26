package issueissyu.backend.domain.alarm.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AlarmListItemResDTO(
        Long alarmId,
        @JsonProperty("isConfirmed") boolean isConfirmed,
        String alarmType,
        String alarmTitle,
        String alarmBody,
        Long pinId,
        Long communityId,
        String timeAgo) {}
