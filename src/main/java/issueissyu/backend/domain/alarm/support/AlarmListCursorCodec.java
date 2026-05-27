package issueissyu.backend.domain.alarm.support;

import issueissyu.backend.domain.alarm.exception.AlarmException;
import issueissyu.backend.domain.alarm.exception.code.AlarmErrorCode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class AlarmListCursorCodec {

    public String encode(long alarmId) {
        if (alarmId <= 0) {
            throw AlarmException.of(AlarmErrorCode.ALARM_LIST_400_2);
        }
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(Long.toString(alarmId).getBytes(StandardCharsets.UTF_8));
    }

    public long decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            throw AlarmException.of(AlarmErrorCode.ALARM_LIST_400_2);
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor.trim()), StandardCharsets.UTF_8);
            long alarmId = Long.parseLong(raw);
            if (alarmId <= 0) {
                throw AlarmException.of(AlarmErrorCode.ALARM_LIST_400_2);
            }
            return alarmId;
        } catch (AlarmException e) {
            throw e;
        } catch (Exception e) {
            throw AlarmException.of(AlarmErrorCode.ALARM_LIST_400_2);
        }
    }
}
