package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.billing.repository.UserEmojiRepository;
import issueissyu.backend.domain.pin.dto.req.ApplyPinEmojiReqDTO;
import issueissyu.backend.domain.pin.dto.res.ApplyPinEmojiResDTO;
import issueissyu.backend.domain.pin.entity.Emoji;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.entity.mapping.PinEmoji;
import issueissyu.backend.domain.pin.exception.PinException;
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

import java.util.Optional;

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
        // 1) 핀/이모지는 즉시 도메인 예외
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> PinException.of(PinErrorCode.PIN_NOT_FOUND));
        Emoji targetEmoji = emojiRepository.findById(request.getEmojiId())
                .orElseThrow(() -> PinException.of(PinErrorCode.EMOJI_NOT_FOUND));

        // 2) 기본 이모지가 아니면 구매(보유) 여부를 반드시 검사
        if (!targetEmoji.isDefault() && !userEmojiRepository.existsByUserUidAndEmojiEmojiId(uid, targetEmoji.getEmojiId())) {
            throw PinException.of(PinErrorCode.EMOJI_NOT_OWNED);
        }

        User user = userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        // 3) pinId+uid의 현재 active=true 반응을 잠그고 토글/교체를 원자적으로 처리
        Optional<PinEmoji> currentActiveOpt = pinEmojiRepository.findActiveByPinIdAndUidForUpdate(pinId, uid);
        Long targetEmojiId = targetEmoji.getEmojiId();

        if (currentActiveOpt.isPresent()) {
            PinEmoji currentActive = currentActiveOpt.get();
            Long currentEmojiId = currentActive.getEmoji().getEmojiId();

            // 같은 이모지를 다시 누르면 선택 해제(active=false)
            if (currentEmojiId.equals(targetEmojiId)) {
                currentActive.deactivate();
                pinEmojiRepository.save(currentActive);
                return ApplyPinEmojiResDTO.builder()
                        .selectedEmojiId(null)
                        .build();
            }

            // 다른 이모지를 누르면 기존 선택은 해제하고 새 선택만 active=true
            currentActive.deactivate();
            pinEmojiRepository.save(currentActive);
        }

        PinEmoji targetRow = pinEmojiRepository.findByPinPinIdAndUserUidAndEmojiEmojiId(pinId, uid, targetEmojiId)
                .orElse(PinEmoji.builder()
                        .pin(pin)
                        .user(user)
                        .emoji(targetEmoji)
                        .active(false)
                        .build());

        targetRow.activate();
        pinEmojiRepository.save(targetRow);

        return ApplyPinEmojiResDTO.builder()
                .selectedEmojiId(targetEmojiId)
                .build();
    }
}
