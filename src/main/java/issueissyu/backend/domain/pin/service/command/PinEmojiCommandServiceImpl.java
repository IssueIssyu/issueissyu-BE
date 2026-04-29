package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.billing.repository.UserEmogjiRepository;
import issueissyu.backend.domain.pin.dto.req.ApplyPinEmojiReqDTO;
import issueissyu.backend.domain.pin.dto.res.ApplyPinEmojiResDTO;
import issueissyu.backend.domain.pin.entity.Emogji;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.entity.mapping.PinEmogji;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.EmogjiRepository;
import issueissyu.backend.domain.pin.repository.PinEmogjiRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PinEmojiCommandServiceImpl implements PinEmojiCommandService {

    private final PinRepository pinRepository;
    private final EmogjiRepository emogjiRepository;
    private final PinEmogjiRepository pinEmogjiRepository;
    private final UserEmogjiRepository userEmogjiRepository;
    private final UserRepository userRepository;

    @Override
    public ApplyPinEmojiResDTO applyMyEmoji(Long pinId, String uid, ApplyPinEmojiReqDTO request) {
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> GeneralException.of(PinErrorCode.PIN_NOT_FOUND));
        Emogji targetEmogji = emogjiRepository.findById(request.getEmogjiId())
                .orElseThrow(() -> GeneralException.of(PinErrorCode.EMOJI_NOT_FOUND));

        if (!targetEmogji.isDefault() && !userEmogjiRepository.existsByUserUidAndEmogjiEmojiId(uid, targetEmogji.getEmojiId())) {
            throw GeneralException.of(PinErrorCode.EMOJI_NOT_OWNED);
        }

        User user = userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        PinEmogji pinEmogji = pinEmogjiRepository.findByPinPinIdAndUserUid(pinId, uid)
                .orElse(PinEmogji.builder()
                        .pin(pin)
                        .user(user)
                        .emogji(targetEmogji)
                        .build());

        if (!pinEmogji.getEmogji().getEmojiId().equals(targetEmogji.getEmojiId())) {
            pinEmogji.changeEmogji(targetEmogji);
        }

        pinEmogjiRepository.save(pinEmogji);

        return ApplyPinEmojiResDTO.builder()
                .emogjiId(targetEmogji.getEmojiId())
                .emojiImageUrl(targetEmogji.getEmojiImageUrl())
                .build();
    }

    @Override
    public void deleteMyEmoji(Long pinId, String uid) {
        if (!pinRepository.existsById(pinId)) {
            throw GeneralException.of(PinErrorCode.PIN_NOT_FOUND);
        }

        PinEmogji pinEmogji = pinEmogjiRepository.findByPinPinIdAndUserUid(pinId, uid)
                .orElseThrow(() -> GeneralException.of(PinErrorCode.MY_EMOJI_NOT_FOUND));

        pinEmogjiRepository.delete(pinEmogji);
    }
}
