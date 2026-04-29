package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.billing.repository.UserEmojiRepository;
import issueissyu.backend.domain.pin.dto.res.EmojiCandidateResDTO;
import issueissyu.backend.domain.pin.dto.res.PinEmojiSummaryResDTO;
import issueissyu.backend.domain.pin.entity.Emoji;
import issueissyu.backend.domain.pin.entity.mapping.PinEmoji;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.EmojiRepository;
import issueissyu.backend.domain.pin.repository.PinEmojiRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.global.exception.GeneralException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PinEmojiQueryServiceImpl implements PinEmojiQueryService {

    private final PinRepository pinRepository;
    private final PinEmojiRepository pinEmojiRepository;
    private final EmojiRepository emojiRepository;
    private final UserEmojiRepository userEmojiRepository;

    @Override
    public List<PinEmojiSummaryResDTO> getPinEmojiSummaries(Long pinId, String uid) {
        //핀 존재 확인
        ensurePinExists(pinId);

        //반응 개수 DB GROUP BY로 집계
        List<PinEmojiRepository.PinEmojiCountProjection> counts =
                pinEmojiRepository.countByPinIdGroupByEmoji(pinId);

        //내 현재 반응 1건 조회하여 isMine 반영
        Optional<PinEmoji> myEmoji = pinEmojiRepository.findByPinPinIdAndUserUid(pinId, uid);
        Long myEmojiId = myEmoji.map(pinEmoji -> pinEmoji.getEmoji().getEmojiId()).orElse(null);

        return counts.stream()
                .map(count -> PinEmojiSummaryResDTO.builder()
                        .emojiId(count.getEmojiId())
                        .emojiImageUrl(count.getEmojiImageUrl())
                        .count((int) count.getCount())
                        .isMine(count.getEmojiId().equals(myEmojiId))
                        .build())
                .sorted(Comparator.comparingInt(PinEmojiSummaryResDTO::getCount).reversed()
                        .thenComparing(PinEmojiSummaryResDTO::getEmojiId))
                .toList();
    }

    @Override
    public List<EmojiCandidateResDTO> getEmojiCandidates(String uid) {
        // 전체 이모지(기본/유료 포함) 목록 조회
        List<Emoji> allEmojis = emojiRepository.findAllByOrderByEmojiIdAsc();

        // 사용자 보유 이모지 id 목록 Set으로 만들어 포함 검사 사용
        Set<Long> ownedEmojiIds = new HashSet<>(userEmojiRepository.findOwnedEmojiIdsByUid(uid));

        // 화면에서 바로 사용할 형태로 변환하여 리스트 반환
        List<EmojiCandidateResDTO> result = new ArrayList<>();
        for (Emoji emoji : allEmojis) {
            boolean isOwned = ownedEmojiIds.contains(emoji.getEmojiId());
            // 이미 사용 가능한 이모지(기본 제공 또는 보유)는 productId 불필요
            String productId = (emoji.isDefault() || isOwned) ? null : emoji.getProductId();
            result.add(EmojiCandidateResDTO.builder()
                    .emojiId(emoji.getEmojiId())
                    .emojiImageUrl(emoji.getEmojiImageUrl())
                    .isDefault(emoji.isDefault())
                    .isOwned(isOwned)
                    .productId(productId)
                    .build());
        }
        return result;
    }

    private void ensurePinExists(Long pinId) {
        if (!pinRepository.existsById(pinId)) {
            throw GeneralException.of(PinErrorCode.PIN_NOT_FOUND);
        }
    }
}
