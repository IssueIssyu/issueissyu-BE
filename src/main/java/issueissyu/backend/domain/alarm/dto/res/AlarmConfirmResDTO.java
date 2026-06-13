package issueissyu.backend.domain.alarm.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AlarmConfirmResDTO(
    Long alarmId, @JsonProperty("isConfirmed") 
    boolean isConfirmed
    ) {}
