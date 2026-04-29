package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.billing.repository.UserEmogjiRepository;
import issueissyu.backend.domain.pin.dto.res.EmojiCandidateResDTO;
import issueissyu.backend.domain.pin.dto.res.PinEmojiSummaryResDTO;
import issueissyu.backend.domain.pin.entity.Emogji;
import issueissyu.backend.domain.pin.entity.mapping.PinEmogji;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.EmogjiRepository;
import issueissyu.backend.domain.pin.repository.PinEmogjiRepository;
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
    private final PinEmogjiRepository pinEmogjiRepository;
    private final EmogjiRepository emogjiRepository;
    private final UserEmogjiRepository userEmogjiRepository;

    @Override
    public List<PinEmojiSummaryResDTO> getPinEmojiSummaries(Long pinId, String uid) {
        // 1) 존재하지 않는 핀에 대한 조회를 먼저 차단한다.
        ensurePinExists(pinId);

        // 2) 반응 개수는 DB GROUP BY로 집계한다. (Java 루프 +1 제거)
        List<PinEmogjiRepository.PinEmojiCountProjection> counts =
                pinEmogjiRepository.countByPinIdGroupByEmoji(pinId);

        // 3) 내 현재 반응 1건을 따로 조회해 isMine 계산에 사용한다.
        Optional<PinEmogji> myEmoji = pinEmogjiRepository.findByPinPinIdAndUserUid(pinId, uid);
        Long myEmojiId = myEmoji.map(pinEmogji -> pinEmogji.getEmogji().getEmojiId()).orElse(null);

        // 4) 집계 결과를 API 응답 DTO로 변환한다.
        List<PinEmojiSummaryResDTO> result = counts.stream()
                .map(count -> PinEmojiSummaryResDTO.builder()
                        .emogjiId(count.getEmogjiId())
                        .emojiImageUrl(count.getEmojiImageUrl())
                        .count((int) count.getCount())
                        .isMine(count.getEmogjiId().equals(myEmojiId))
                        .build())
                .toList();

        // 5) count 내림차순, 동률이면 emojiId 오름차순 정렬한다.
        result.sort(Comparator.comparingInt(PinEmojiSummaryResDTO::getCount).reversed()
                .thenComparing(PinEmojiSummaryResDTO::getEmogjiId));
        return result;
    }

    @Override
    public List<EmojiCandidateResDTO> getEmojiCandidates(Long pinId, String uid) {
        // 1) 존재하지 않는 핀 조회를 차단한다.
        ensurePinExists(pinId);

        // 2) 전체 이모지(기본/유료 포함) 목록을 읽는다.
        List<Emogji> allEmojis = emogjiRepository.findAllByOrderByEmojiIdAsc();

        // 3) 사용자 보유 이모지 id 목록을 Set으로 만들어 O(1) 포함 검사에 사용한다.
        Set<Long> ownedEmojiIds = new HashSet<>(userEmogjiRepository.findOwnedEmojiIdsByUid(uid));

        // 4) 현재 핀에서 내 반응 1건을 찾아 isMine 계산에 사용한다.
        Optional<PinEmogji> myEmoji = pinEmogjiRepository.findByPinPinIdAndUserUid(pinId, uid);
        Long myEmojiId = myEmoji.map(pinEmogji -> pinEmogji.getEmogji().getEmojiId()).orElse(null);

        // 5) 후보 리스트를 화면에서 바로 사용할 형태로 변환한다.
        List<EmojiCandidateResDTO> result = new ArrayList<>();
        for (Emogji emogji : allEmojis) {
            boolean isOwned = ownedEmojiIds.contains(emogji.getEmojiId());
            boolean isMine = emogji.getEmojiId().equals(myEmojiId);
            result.add(EmojiCandidateResDTO.builder()
                    .emogjiId(emogji.getEmojiId())
                    .emojiImageUrl(emogji.getEmojiImageUrl())
                    .isDefault(emogji.isDefault())
                    .isOwned(isOwned)
                    .isMine(isMine)
                    .build());
        }
        return result;
    }

    private void ensurePinExists(Long pinId) {
        // 핀이 없으면 이후 쿼리/변환 로직을 진행하지 않고 즉시 실패시킨다.
        if (!pinRepository.existsById(pinId)) {
            throw GeneralException.of(PinErrorCode.PIN_NOT_FOUND);
        }
    }
}
