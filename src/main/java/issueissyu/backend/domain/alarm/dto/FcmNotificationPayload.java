package issueissyu.backend.domain.alarm.dto;

import issueissyu.backend.domain.alarm.enums.AlarmPushType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record FcmNotificationPayload(String token, String title, String body, Map<String, String> data) {

    public static Builder builder(String token, String title, String body) {
        return new Builder(token, title, body);
    }

    public static final class Builder {

        private final String token;
        private final String title;
        private final String body;
        private AlarmPushType pushType;
        private final Map<String, String> entries = new LinkedHashMap<>();

        private Builder(String token, String title, String body) {
            this.token = token;
            this.title = title;
            this.body = body;
        }

        public Builder pushType(AlarmPushType pushType) {
            this.pushType = pushType;
            return this;
        }

        public Builder put(String key, String value) {
            if (key != null && value != null) {
                entries.put(key, value);
            }
            return this;
        }

        public FcmNotificationPayload build() {
            Objects.requireNonNull(pushType, "pushType");
            Map<String, String> data = new LinkedHashMap<>(entries);
            data.put("type", pushType.fcmType());
            return new FcmNotificationPayload(token, title, body, Map.copyOf(data));
        }
    }
}
