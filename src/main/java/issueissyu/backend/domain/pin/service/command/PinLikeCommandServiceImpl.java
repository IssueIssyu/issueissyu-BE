package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.alarm.entity.LikeAlarm;
import issueissyu.backend.domain.alarm.entity.UserAlarm;
import issueissyu.backend.domain.alarm.event.LikeAlarmCreatedEvent;
import issueissyu.backend.domain.alarm.repository.LikeAlarmRepository;
import issueissyu.backend.domain.alarm.repository.UserAlarmRepository;
import issueissyu.backend.domain.pin.dto.res.PinLikeResDTO;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.entity.mapping.PinLike;
import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.PinLikeRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PinLikeCommandServiceImpl implements PinLikeCommandService {

    private static final String LIKE_ALARM_TITLE = "💝 공감 도착!";
    private static final String LIKE_ALARM_BODY_TEMPLATE = "%s님이 내 %s에 좋아요를 눌렀어요.";

    private final PinRepository pinRepository;
    private final PinLikeRepository pinLikeRepository;
    private final UserRepository userRepository;
    private final UserAlarmRepository userAlarmRepository;
    private final LikeAlarmRepository likeAlarmRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PinLikeResDTO likePin(Long pinId, String uid) {
        User liker =
                userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
        Pin pin = pinRepository
                .findWithPessimisticWriteByPinId(pinId)
                .orElseThrow(() -> PinException.of(PinErrorCode.PIN_NOT_FOUND));

        if (pinLikeRepository.existsByPin_PinIdAndUser_Uid(pinId, uid)) {
            throw PinException.of(PinErrorCode.PIN_LIKE_ALREADY);
        }

        PinLike pinLike = PinLike.builder().pin(pin).user(liker).build();
        try {
            pinLikeRepository.saveAndFlush(pinLike);
        } catch (DataIntegrityViolationException e) {
            throw PinException.of(PinErrorCode.PIN_LIKE_ALREADY);
        }

        pinRepository.incrementLikeCountByPinId(pinId);

        int likeCount = pinRepository.findLikeCountByPinId(pinId).orElse(pin.getLikeCount());

        User pinOwner = pin.getUser();
        if (!pinOwner.getUid().equals(uid)) {
            publishLikeAlarmEventIfEligible(pinOwner, liker.getNickname(), pin.getPinTitle());
        }

        return PinLikeResDTO.builder()
                .pinId(pinId)
                .pinLikeCount(likeCount)
                .isLike(true)
                .build();
    }

    private void publishLikeAlarmEventIfEligible(User recipient, String likerNickname, String pinTitle) {
        if (!recipient.isLikeAlarmActive()) {
            return;
        }

        String body = String.format(LIKE_ALARM_BODY_TEMPLATE, likerNickname, pinTitle);

        UserAlarm userAlarm = userAlarmRepository.save(
                UserAlarm.builder().user(recipient).build());

        LikeAlarm likeAlarm = likeAlarmRepository.save(
                LikeAlarm.builder()
                        .userAlarm(userAlarm)
                        .likeAlarmTitle(LIKE_ALARM_TITLE)
                        .likeAlarmBody(body)
                        .build());

        String pushToken = recipient.getPushToken();
        if (!StringUtils.hasText(pushToken)) {
            return;
        }

        eventPublisher.publishEvent(new LikeAlarmCreatedEvent(
                recipient.getUid(), pushToken, likeAlarm.getLikeAlarmId(), LIKE_ALARM_TITLE, body));
    }
}
