package issueissyu.backend.domain.map.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import issueissyu.backend.domain.issue.enums.IssuePinState;
import issueissyu.backend.domain.map.exception.MapException;
import issueissyu.backend.domain.map.exception.code.MapErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PatchNoteCursorCodec {

    private static final DateTimeFormatter CURSOR_TIME =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 6, 9, true)
                    .toFormatter();

    private final ObjectMapper objectMapper;

    public record Decoded(int rank, LocalDateTime createdAt, long pinId, IssuePinState state) {}

    public String encode(IssuePinState state, LocalDateTime createdAt, long pinId) {
        try {
            CursorJson json = new CursorJson(state.name(), createdAt.format(CURSOR_TIME), pinId);
            String raw = objectMapper.writeValueAsString(json);
            return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw MapException.of(MapErrorCode.PATCHNOTE_400_5);
        }
    }

    public Decoded decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            throw MapException.of(MapErrorCode.PATCHNOTE_400_3);
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(cursor.trim());
            CursorJson json = objectMapper.readValue(decoded, CursorJson.class);
            if (json.s == null || json.c == null || json.i == null) {
                throw MapException.of(MapErrorCode.PATCHNOTE_400_3);
            }
            IssuePinState state = IssuePinState.valueOf(json.s);
            LocalDateTime createdAt = LocalDateTime.parse(json.c, CURSOR_TIME);
            long pinId = json.i;
            if (pinId <= 0) {
                throw MapException.of(MapErrorCode.PATCHNOTE_400_3);
            }
            int rank =
                    switch (state) {
                        case BEFORE_PROGRESS -> 0;
                        case IN_PROGRESS -> 1;
                        case RESOLVED -> 2;
                    };
            return new Decoded(rank, createdAt, pinId, state);
        } catch (MapException e) {
            throw e;
        } catch (Exception e) {
            throw MapException.of(MapErrorCode.PATCHNOTE_400_3);
        }
    }

    private record CursorJson(String s, String c, Long i) {}
}
