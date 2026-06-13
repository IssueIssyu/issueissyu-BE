package issueissyu.backend.domain.user.service.query;

import issueissyu.backend.domain.alarm.exception.AlarmException;
import issueissyu.backend.domain.alarm.exception.code.AlarmErrorCode;
import issueissyu.backend.domain.user.dto.res.UserAlarmStateResDTO;
import issueissyu.backend.domain.user.repository.UserAlarmStateRow;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAlarmStateQueryServiceImpl implements UserAlarmStateQueryService {

    private final UserRepository userRepository;

    @Override
    public UserAlarmStateResDTO getAlarmState(String uid) {
        UserAlarmStateRow row = userRepository
                .findAlarmStateByUid(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        Boolean likeAlarmActive = row.getLikeAlarmActive();
        Boolean eventAlarmActive = row.getEventAlarmActive();
        Boolean hotAlarmActive = row.getHotAlarmActive();
        Boolean storeAlarmActive = row.getStoreAlarmActive();

        if (likeAlarmActive == null
                || eventAlarmActive == null
                || hotAlarmActive == null
                || storeAlarmActive == null) {
            throw AlarmException.of(AlarmErrorCode.ALARM_STATE_400);
        }

        return new UserAlarmStateResDTO(
                likeAlarmActive, eventAlarmActive, hotAlarmActive, storeAlarmActive);
    }
}
