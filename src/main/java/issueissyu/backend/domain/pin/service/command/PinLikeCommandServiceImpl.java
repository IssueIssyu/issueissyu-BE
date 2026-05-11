package issueissyu.backend.domain.pin.service.command;

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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PinLikeCommandServiceImpl implements PinLikeCommandService {

    private final PinRepository pinRepository;
    private final PinLikeRepository pinLikeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PinLikeResDTO likePin(Long pinId, String uid) {
        User user =
                userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
        Pin pin = pinRepository
                .findWithPessimisticWriteByPinId(pinId)
                .orElseThrow(() -> PinException.of(PinErrorCode.PIN_NOT_FOUND));

        if (pinLikeRepository.existsByPin_PinIdAndUser_Uid(pinId, uid)) {
            throw PinException.of(PinErrorCode.PIN_LIKE_ALREADY);
        }

        PinLike pinLike = PinLike.builder().pin(pin).user(user).build();
        try {
            pinLikeRepository.saveAndFlush(pinLike);
        } catch (DataIntegrityViolationException e) {
            throw PinException.of(PinErrorCode.PIN_LIKE_ALREADY);
        }

        pinRepository.incrementLikeCountByPinId(pinId);

        int likeCount =
                pinRepository.findLikeCountByPinId(pinId).orElse(pin.getLikeCount());

        return PinLikeResDTO.builder()
                .pinId(pinId)
                .pinLikeCount(likeCount)
                .isLike(true)
                .build();
    }
}
