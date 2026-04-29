package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.billing.repository.UserEmojiRepository;
import issueissyu.backend.domain.pin.dto.req.ApplyPinEmojiReqDTO;
import issueissyu.backend.domain.pin.dto.res.ApplyPinEmojiResDTO;
import issueissyu.backend.domain.pin.entity.Emoji;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.entity.mapping.PinEmoji;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.EmojiRepository;
import issueissyu.backend.domain.pin.repository.PinEmojiRepository;
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
    private final EmojiRepository emojiRepository;
    private final PinEmojiRepository pinEmojiRepository;
    private final UserEmojiRepository userEmojiRepository;
    private final UserRepository userRepository;

    @Override
    public ApplyPinEmojiResDTO applyMyEmoji(Long pinId, String uid, ApplyPinEmojiReqDTO request) {
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> GeneralException.of(PinErrorCode.PIN_NOT_FOUND));
        Emoji targetEmoji = emojiRepository.findById(request.getEmojiId())
                .orElseThrow(() -> GeneralException.of(PinErrorCode.EMOJI_NOT_FOUND));

        // 기본 이모지 아니면 보유 여부 확인 필요
        if (!targetEmoji.isDefault() && !userEmojiRepository.existsByUserUidAndEmojiEmojiId(uid, targetEmoji.getEmojiId())) {
            throw GeneralException.of(PinErrorCode.EMOJI_NOT_OWNED);
        }

        User user = userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        // 핀_이모지 조회 없으면 생성
        PinEmoji pinEmoji = pinEmojiRepository.findByPinPinIdAndUserUid(pinId, uid)
                .orElse(PinEmoji.builder()
                        .pin(pin)
                        .user(user)
                        .emoji(targetEmoji)
                        .build());

        // 이모지 변경 필요 여부 확인 및 변경
        if (!pinEmoji.getEmoji().getEmojiId().equals(targetEmoji.getEmojiId())) {
            pinEmoji.changeEmoji(targetEmoji);
        }

        pinEmojiRepository.save(pinEmoji);

        // 응답 반환
        return ApplyPinEmojiResDTO.builder()
                .emojiId(targetEmoji.getEmojiId())
                .emojiImageUrl(targetEmoji.getEmojiImageUrl())
                .build();
    }

    @Override
    public void deleteMyEmoji(Long pinId, String uid) {
        if (!pinRepository.existsById(pinId)) {
            throw GeneralException.of(PinErrorCode.PIN_NOT_FOUND);
        }

        PinEmoji pinEmoji = pinEmojiRepository.findByPinPinIdAndUserUid(pinId, uid)
                .orElseThrow(() -> GeneralException.of(PinErrorCode.MY_EMOJI_NOT_FOUND));

        pinEmojiRepository.delete(pinEmoji);
    }
}
