package issueissyu.backend.domain.alarm.dto;

import java.util.Map;

public record FcmNotificationPayload(
        String token, 
        String title, 
        String body, 
        Map<String, String> data
        ) {}
