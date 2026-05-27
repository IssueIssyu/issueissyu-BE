package issueissyu.backend.domain.alarm.service.query;

import issueissyu.backend.domain.alarm.converter.UserAlarmConverter;
import issueissyu.backend.domain.alarm.dto.res.AlarmListItemResDTO;
import issueissyu.backend.domain.alarm.dto.res.AlarmListPageInfoResDTO;
import issueissyu.backend.domain.alarm.dto.res.AlarmListResDTO;
import issueissyu.backend.domain.alarm.exception.AlarmException;
import issueissyu.backend.domain.alarm.exception.code.AlarmErrorCode;
import issueissyu.backend.domain.alarm.repository.UserAlarmListRepository;
import issueissyu.backend.domain.alarm.repository.UserAlarmListRow;
import issueissyu.backend.domain.alarm.support.AlarmListCursorCodec;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAlarmQueryServiceImpl implements UserAlarmQueryService {

    private static final int SIZE_DEFAULT = 10;
    private static final int SIZE_MIN = 1;
    private static final int SIZE_MAX = 100;

    private final UserAlarmListRepository userAlarmListRepository;
    private final AlarmListCursorCodec alarmListCursorCodec;
    private final UserAlarmConverter userAlarmConverter;

    @Override
    public AlarmListResDTO getAlarmList(String uid, Integer size, String cursor) {
        int pageSize = resolveSize(size);

        boolean applyCursor = StringUtils.hasText(cursor);
        long cursorAlarmId = 0L;
        if (applyCursor) {
            cursorAlarmId = alarmListCursorCodec.decode(cursor.trim());
        }

        List<UserAlarmListRow> rows =
                userAlarmListRepository.findAlarmList(uid, applyCursor, cursorAlarmId, pageSize + 1);

        boolean hasNext = rows.size() > pageSize;
        List<UserAlarmListRow> pageRows = hasNext ? rows.subList(0, pageSize) : rows;

        LocalDateTime now = LocalDateTime.now();
        List<AlarmListItemResDTO> alarms =
                pageRows.stream().map(row -> userAlarmConverter.toListItem(row, now)).toList();

        String nextCursor = null;
        if (hasNext && !pageRows.isEmpty()) {
            nextCursor = alarmListCursorCodec.encode(pageRows.get(pageRows.size() - 1).getAlarmId());
        }

        return new AlarmListResDTO(alarms, new AlarmListPageInfoResDTO(hasNext, nextCursor));
    }

    @Override
    public AlarmListItemResDTO getAlarm(String uid, Long alarmId) {
        UserAlarmListRow row = userAlarmListRepository
                .findAlarmDetail(uid, alarmId)
                .orElseThrow(() -> AlarmException.of(AlarmErrorCode.ALARM_404));

        return userAlarmConverter.toListItem(row, LocalDateTime.now());
    }

    private int resolveSize(Integer size) {
        int resolved = size == null ? SIZE_DEFAULT : size;
        if (resolved < SIZE_MIN || resolved > SIZE_MAX) {
            throw AlarmException.of(AlarmErrorCode.ALARM_LIST_400_1);
        }
        return resolved;
    }
}
