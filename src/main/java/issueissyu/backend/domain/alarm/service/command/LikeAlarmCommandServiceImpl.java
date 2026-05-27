package issueissyu.backend.domain.alarm.service.command;

import issueissyu.backend.domain.alarm.entity.LikeAlarm;
import issueissyu.backend.domain.alarm.entity.UserAlarm;
import issueissyu.backend.domain.alarm.exception.AlarmException;
import issueissyu.backend.domain.alarm.exception.code.AlarmErrorCode;
import issueissyu.backend.domain.alarm.repository.LikeAlarmRepository;
import issueissyu.backend.domain.alarm.repository.UserAlarmRepository;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.repository.PinLikeRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeAlarmCommandServiceImpl implements LikeAlarmCommandService {

    private static final String LIKE_ALARM_TITLE = "💝 공감 도착!";
    private static final String LIKE_ALARM_BODY_TEMPLATE = "%s님이 내 %s에 좋아요를 눌렀어요.";

    private final UserRepository userRepository;
    private final PinRepository pinRepository;
    private final PinLikeRepository pinLikeRepository;
    private final UserAlarmRepository userAlarmRepository;
    private final LikeAlarmRepository likeAlarmRepository;

    @Override
    @Transactional
    public LikeAlarmPrepared createLikeAlarmForApi(String likerUid, Long pinId) {
        LikeContext context = resolveLikeContext(likerUid, pinId);

        if (context.recipient().getUid().equals(likerUid)) {
            throw AlarmException.of(AlarmErrorCode.LIKE_ALARM_400);
        }

        if (!context.recipient().isLikeAlarmActive()) {
            throw AlarmException.of(AlarmErrorCode.LIKE_ALARM_403);
        }

        return persistLikeAlarm(context);
    }

    @Override
    @Transactional
    public Optional<LikeAlarmPrepared> createLikeAlarmIfEligible(String likerUid, Long pinId) {
        LikeContext context = resolveLikeContext(likerUid, pinId);

        if (context.recipient().getUid().equals(likerUid)) {
            return Optional.empty();
        }

        if (!context.recipient().isLikeAlarmActive()) {
            return Optional.empty();
        }

        return Optional.of(persistLikeAlarm(context));
    }

    private LikeContext resolveLikeContext(String likerUid, Long pinId) {
        User liker = userRepository
                .findById(likerUid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        Pin pin = pinRepository
                .findById(pinId)
                .orElseThrow(() -> AlarmException.of(AlarmErrorCode.LIKE_ALARM_404));

        if (!pinLikeRepository.existsByPin_PinIdAndUser_Uid(pinId, likerUid)) {
            throw AlarmException.of(AlarmErrorCode.LIKE_ALARM_400);
        }

        return new LikeContext(liker, pin, pin.getUser());
    }

    private LikeAlarmPrepared persistLikeAlarm(LikeContext context) {
        String body = String.format(
                LIKE_ALARM_BODY_TEMPLATE, context.liker().getNickname(), context.pin().getPinTitle());

        UserAlarm userAlarm = userAlarmRepository.save(
                UserAlarm.builder().user(context.recipient()).build());

        LikeAlarm likeAlarm = likeAlarmRepository.save(
                LikeAlarm.builder()
                        .userAlarm(userAlarm)
                        .likeAlarmTitle(LIKE_ALARM_TITLE)
                        .likeAlarmBody(body)
                        .likePinId(context.pin().getPinId())
                        .build());

        return new LikeAlarmPrepared(
                likeAlarm.getLikeAlarmId(),
                context.pin().getPinId(),
                context.recipient().getUid(),
                context.recipient().getPushToken(),
                LIKE_ALARM_TITLE,
                body);
    }

    private record LikeContext(User liker, Pin pin, User recipient) {}
}
