package issueissyu.backend.domain.alarm.dto.res;

public record EventAlarmSendResDTO(
        Long eventAlarmId, 
        String messageId, 
        Long pinId, 
        Long communityId
        ) {}
