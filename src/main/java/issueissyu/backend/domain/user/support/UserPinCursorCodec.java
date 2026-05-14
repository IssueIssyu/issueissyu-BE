package issueissyu.backend.domain.user.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import issueissyu.backend.domain.user.exception.UserException;
import issueissyu.backend.domain.user.exception.code.UserErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPinCursorCodec {

    private static final DateTimeFormatter CURSOR_TIME =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 6, 9, true)
                    .toFormatter();

    private final ObjectMapper objectMapper;

    public record Decoded(LocalDateTime createdAt, long pinId) {}

    public String encode(LocalDateTime createdAt, long pinId) {
        try {
            CursorJson json = new CursorJson(createdAt.format(CURSOR_TIME), pinId);
            String raw = objectMapper.writeValueAsString(json);
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw UserException.of(UserErrorCode.USER_PIN_400_2);
        }
    }

    public Decoded decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            throw UserException.of(UserErrorCode.USER_PIN_400_2);
        }
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor.trim());
            CursorJson json = objectMapper.readValue(decoded, CursorJson.class);
            if (json.c == null || json.i == null) {
                throw UserException.of(UserErrorCode.USER_PIN_400_2);
            }
            LocalDateTime createdAt = LocalDateTime.parse(json.c, CURSOR_TIME);
            long pinId = json.i;
            if (pinId <= 0) {
                throw UserException.of(UserErrorCode.USER_PIN_400_2);
            }
            return new Decoded(createdAt, pinId);
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw UserException.of(UserErrorCode.USER_PIN_400_2);
        }
    }

    private record CursorJson(String c, Long i) {}
}
