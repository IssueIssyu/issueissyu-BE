package issueissyu.backend.domain.alarm.dto.res;

public record HotAlarmSendResDTO(
        Long hotAlarmId, 
        String messageId, 
        Long pinId, 
        Long communityId
        ) {}
