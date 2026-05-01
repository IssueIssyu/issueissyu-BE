package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.billing.repository.UserEmojiRepository;
import issueissyu.backend.domain.pin.dto.res.EmojiCandidateResDTO;
import issueissyu.backend.domain.pin.dto.res.PinEmojiSummaryListResDTO;
import issueissyu.backend.domain.pin.dto.res.PinEmojiSummaryResDTO;
import issueissyu.backend.domain.pin.entity.Emoji;
import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.EmojiRepository;
import issueissyu.backend.domain.pin.repository.PinEmojiRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    public PinEmojiSummaryListResDTO getPinEmojiSummaries(Long pinId, String uid) {
        // 1) 핀 존재 여부 확인
        ensurePinExists(pinId);

        // 2) active=true 집계 -> 반응 수
        List<PinEmojiRepository.PinEmojiCountProjection> counts =
                pinEmojiRepository.countByPinIdGroupByEmoji(pinId);
        Map<Long, Long> countByEmojiId = new HashMap<>();
        for (PinEmojiRepository.PinEmojiCountProjection count : counts) {
            countByEmojiId.put(count.getEmojiId(), count.getCount());
        }

        // 3) pinId+uid 기준 active=true 1건 = selectedEmojiId
        Long selectedEmojiId = pinEmojiRepository.findByPinPinIdAndUserUidAndActiveTrue(pinId, uid)
                .map(pinEmoji -> pinEmoji.getEmoji().getEmojiId())
                .orElse(null);

        // 4) 구매 상태/상품 정보
        List<Emoji> allEmojis = emojiRepository.findAllByOrderByEmojiIdAsc();
        Set<Long> ownedEmojiIds = new HashSet<>(userEmojiRepository.findOwnedEmojiIdsByUid(uid));

        List<PinEmojiSummaryResDTO> emojis = new ArrayList<>();
        for (Emoji emoji : allEmojis) {
            long count = countByEmojiId.getOrDefault(emoji.getEmojiId(), 0L);
            // 기본 이모지는 count=0이어도 항상 노출, 그 외 이모지는 해당 핀에 반응이 있을 때만 노출
            if (!emoji.isDefault() && count == 0L) {
                continue;
            }

            EmojiAvailability availability = resolveAvailability(emoji, ownedEmojiIds);

            emojis.add(PinEmojiSummaryResDTO.builder()
                    .emojiId(emoji.getEmojiId())
                    .emojiImageUrl(emoji.getEmojiImageUrl())
                    .count((int) count)
                    .isDefault(emoji.isDefault())
                    .isOwned(availability.isOwned())
                    .productId(availability.productId())
                    .build());
        }

        // 5) 1순위: count 내림차순, 2순위: emojiId 오름차순 으로 정렬
        emojis.sort(Comparator.comparingInt(PinEmojiSummaryResDTO::getCount).reversed()
                .thenComparing(PinEmojiSummaryResDTO::getEmojiId));

        return PinEmojiSummaryListResDTO.builder()
                .selectedEmojiId(selectedEmojiId)
                .emojis(emojis)
                .build();
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
            EmojiAvailability availability = resolveAvailability(emoji, ownedEmojiIds);
            result.add(EmojiCandidateResDTO.builder()
                    .emojiId(emoji.getEmojiId())
                    .emojiImageUrl(emoji.getEmojiImageUrl())
                    .isDefault(emoji.isDefault())
                    .isOwned(availability.isOwned())
                    .productId(availability.productId())
                    .build());
        }
        return result;
    }

    // 두 API(핀 상세/이모지 피커)에서 동일하게 쓰는 구매 가능 상태 계산 로직.
    private EmojiAvailability resolveAvailability(Emoji emoji, Set<Long> ownedEmojiIds) {
        boolean isOwned = ownedEmojiIds.contains(emoji.getEmojiId());
        String productId = (emoji.isDefault() || isOwned) ? null : emoji.getProductId();
        return new EmojiAvailability(isOwned, productId);
    }

    private void ensurePinExists(Long pinId) {
        if (!pinRepository.existsById(pinId)) {
            throw PinException.of(PinErrorCode.PIN_NOT_FOUND);
        }
    }

    private record EmojiAvailability(boolean isOwned, String productId) {
    }
}
