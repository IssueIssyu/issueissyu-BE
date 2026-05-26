package issueissyu.backend.domain.alarm.dto.res;

public record AlarmListPageInfoResDTO(
    boolean hasNext, 
    String nextCursor
    ) {}
