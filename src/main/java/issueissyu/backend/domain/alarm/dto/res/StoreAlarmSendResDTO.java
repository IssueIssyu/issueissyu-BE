package issueissyu.backend.domain.alarm.dto.res;

public record StoreAlarmSendResDTO(
        Long storeAlarmId, 
        String messageId, 
        Long pinId, 
        Long communityId
        ) {}
