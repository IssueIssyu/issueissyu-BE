package issueissyu.backend.domain.user.support;

import issueissyu.backend.domain.user.exception.UserException;
import issueissyu.backend.domain.user.exception.code.UserErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class UserSolverCursorCodec {

    private static final DateTimeFormatter CURSOR_TIME =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 6, 9, true)
                    .toFormatter();

    public record Decoded(LocalDateTime createdAt, long problemSolverId) {}

    public String encode(LocalDateTime createdAt, long problemSolverId) {
        String raw = createdAt.format(CURSOR_TIME) + ":" + problemSolverId;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public Decoded decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            throw UserException.of(UserErrorCode.USER_SOLVER_400_2);
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor.trim()), StandardCharsets.UTF_8);
            int delim = raw.lastIndexOf(':');
            if (delim < 0 || delim == raw.length() - 1) {
                throw UserException.of(UserErrorCode.USER_SOLVER_400_2);
            }
            String createdAtRaw = raw.substring(0, delim);
            String problemSolverIdRaw = raw.substring(delim + 1);
            long problemSolverId = Long.parseLong(problemSolverIdRaw);
            if (problemSolverId <= 0) {
                throw UserException.of(UserErrorCode.USER_SOLVER_400_2);
            }
            LocalDateTime createdAt = LocalDateTime.parse(createdAtRaw, CURSOR_TIME);
            return new Decoded(createdAt, problemSolverId);
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw UserException.of(UserErrorCode.USER_SOLVER_400_2);
        }
    }
}
