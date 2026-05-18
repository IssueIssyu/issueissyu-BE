package issueissyu.backend.domain.alarm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    public String sendNotification(String targetToken, String title, String body, Map<String, String> data) {
        try {
            Message.Builder builder = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());
            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }
            String messageId = FirebaseMessaging.getInstance().send(builder.build());
            log.info("FCM sent: {}", messageId);
            return messageId;
        } catch (Exception e) {
            log.error("FCM send failed token={}: {}", targetToken, e.getMessage(), e);
            throw new RuntimeException("FCM send failed: " + e.getMessage(), e);
        }
    }
}
