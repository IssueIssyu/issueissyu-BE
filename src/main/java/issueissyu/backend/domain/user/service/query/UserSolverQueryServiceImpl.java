package issueissyu.backend.domain.user.service.query;

import issueissyu.backend.domain.user.dto.res.UserMyPinPageInfoResDTO;
import issueissyu.backend.domain.user.dto.res.UserMySolverItemResDTO;
import issueissyu.backend.domain.user.dto.res.UserMySolversResDTO;
import issueissyu.backend.domain.user.exception.UserException;
import issueissyu.backend.domain.user.exception.code.UserErrorCode;
import issueissyu.backend.domain.user.repository.UserMySolverRepository;
import issueissyu.backend.domain.user.repository.UserMySolverRow;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.domain.user.support.UserSolverCursorCodec;
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
public class UserSolverQueryServiceImpl implements UserSolverQueryService {

    private static final int SIZE_DEFAULT = 10;
    private static final int SIZE_MIN = 1;
    private static final int SIZE_MAX = 100;

    private static final LocalDateTime CURSOR_DUMMY_TIME = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

    private final UserRepository userRepository;
    private final UserMySolverRepository userMySolverRepository;
    private final UserSolverCursorCodec userSolverCursorCodec;

    @Override
    public UserMySolversResDTO getMySolvers(String uid, Integer size, String cursor) {
        int pageSize = resolveSize(size);

        if (!userRepository.existsById(uid)) {
            throw GeneralException.of(GeneralErrorCode.USER_NOT_FOUND);
        }

        boolean applyCursor = StringUtils.hasText(cursor);
        LocalDateTime cursorCreatedAt = CURSOR_DUMMY_TIME;
        long cursorProblemSolverId = 0L;
        if (applyCursor) {
            UserSolverCursorCodec.Decoded decoded = userSolverCursorCodec.decode(cursor.trim());
            cursorCreatedAt = decoded.createdAt();
            cursorProblemSolverId = decoded.problemSolverId();
        }

        List<UserMySolverRow> rows =
                userMySolverRepository.findMySolvers(
                        uid, applyCursor, cursorCreatedAt, cursorProblemSolverId, pageSize + 1);

        boolean hasNext = rows.size() > pageSize;
        List<UserMySolverRow> pageRows = hasNext ? rows.subList(0, pageSize) : rows;

        List<UserMySolverItemResDTO> pins =
                pageRows.stream()
                        .map(
                                r ->
                                        new UserMySolverItemResDTO(
                                                r.getPinId(),
                                                r.getPinTitle(),
                                                r.getPinDetailAddress(),
                                                r.getIssuePinState(),
                                                r.getCreatedAt()))
                        .toList();

        String nextCursor = null;
        if (hasNext && !pageRows.isEmpty()) {
            UserMySolverRow last = pageRows.get(pageRows.size() - 1);
            nextCursor = userSolverCursorCodec.encode(last.getCreatedAt(), last.getProblemSolverId());
        }

        return new UserMySolversResDTO(pins, new UserMyPinPageInfoResDTO(hasNext, nextCursor));
    }

    private int resolveSize(Integer size) {
        int s = size == null ? SIZE_DEFAULT : size;
        if (s < SIZE_MIN || s > SIZE_MAX) {
            throw UserException.of(UserErrorCode.USER_SOLVER_400_1);
        }
        return s;
    }
}
