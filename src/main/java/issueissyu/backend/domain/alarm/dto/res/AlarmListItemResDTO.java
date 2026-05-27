package issueissyu.backend.domain.alarm.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record AlarmListItemResDTO(
        Long alarmId,
        @JsonProperty("isConfirmed") boolean isConfirmed,
        String alarmType,
        String alarmTitle,
        String alarmBody,
        Long pinId,
        Long communityId,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS") LocalDateTime createdAt,
        String timeAgo) {}
