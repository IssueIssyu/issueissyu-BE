package issueissyu.backend.domain.user.service.query;

import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.user.dto.res.UserMyPinItemResDTO;
import issueissyu.backend.domain.user.dto.res.UserMyPinPageInfoResDTO;
import issueissyu.backend.domain.user.dto.res.UserMyPinsResDTO;
import issueissyu.backend.domain.user.exception.UserException;
import issueissyu.backend.domain.user.exception.code.UserErrorCode;
import issueissyu.backend.domain.user.repository.UserMyPinRepository;
import issueissyu.backend.domain.user.repository.UserMyPinRow;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.domain.user.support.UserPinCursorCodec;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPinQueryServiceImpl implements UserPinQueryService {

    private static final int SIZE_DEFAULT = 10;
    private static final int SIZE_MIN = 1;
    private static final int SIZE_MAX = 100;

    private static final LocalDateTime CURSOR_DUMMY_TIME = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

    private final UserRepository userRepository;
    private final UserMyPinRepository userMyPinRepository;
    private final UserPinCursorCodec userPinCursorCodec;

    @Override
    public UserMyPinsResDTO getMyPins(String uid, Integer size, String cursor) {
        int pageSize = resolveSize(size);

        userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        boolean applyCursor = StringUtils.hasText(cursor);
        LocalDateTime cursorCreatedAt = CURSOR_DUMMY_TIME;
        long cursorPinId = 0L;
        if (applyCursor) {
            UserPinCursorCodec.Decoded decoded = userPinCursorCodec.decode(cursor.trim());
            cursorCreatedAt = decoded.createdAt();
            cursorPinId = decoded.pinId();
        }

        List<UserMyPinRow> rows =
                userMyPinRepository.findMyPins(uid, applyCursor, cursorCreatedAt, cursorPinId, pageSize + 1);

        boolean hasNext = rows.size() > pageSize;
        List<UserMyPinRow> pageRows = hasNext ? rows.subList(0, pageSize) : rows;

        List<UserMyPinItemResDTO> pins =
                pageRows.stream()
                        .map(
                                r -> {
                                    String pinType = r.getPinType();
                                    String state =
                                            PinType.ISSUE.name().equals(pinType)
                                                    ? r.getIssuePinState()
                                                    : null;
                                    return new UserMyPinItemResDTO(
                                            r.getPinId(),
                                            pinType,
                                            r.getPinDetailAddress(),
                                            state,
                                            r.getCreatedAt());
                                })
                        .toList();

        String nextCursor = null;
        if (hasNext && !pageRows.isEmpty()) {
            UserMyPinRow last = pageRows.get(pageRows.size() - 1);
            nextCursor = userPinCursorCodec.encode(last.getCreatedAt(), last.getPinId());
        }

        return new UserMyPinsResDTO(pins, new UserMyPinPageInfoResDTO(hasNext, nextCursor));
    }

    private int resolveSize(Integer size) {
        int s = size == null ? SIZE_DEFAULT : size;
        if (s < SIZE_MIN || s > SIZE_MAX) {
            throw UserException.of(UserErrorCode.USER_PIN_400_1);
        }
        return s;
    }
}
