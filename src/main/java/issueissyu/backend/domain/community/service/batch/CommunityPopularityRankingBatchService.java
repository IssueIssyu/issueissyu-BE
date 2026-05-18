package issueissyu.backend.domain.community.service.batch;

import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.pin.repository.CommentRepository;
import issueissyu.backend.domain.pin.repository.PinEmojiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityPopularityRankingBatchService {

    // 각 지표에 곱해질 가중치 상수 — 나중에 조정할 때 여기만 바꾸면 돼
    private static final double VIEW_WEIGHT    = 0.1;   // 조회수는 어뷰징 가능성이 있어서 가중치 낮게
    private static final double LIKE_WEIGHT    = 0.4;   // 공감은 명시적 반응이라 높게
    private static final double EMOJI_WEIGHT   = 0.2;   // 이모지는 공감보다 가벼운 반응
    private static final double COMMENT_WEIGHT = 0.3;   // 댓글은 적극적 참여라 높게

    private final CommunityRepository communityRepository;
    private final PinEmojiRepository pinEmojiRepository;
    private final CommentRepository commentRepository;

    // 매일 새벽 4시에 실행 — cron 표현식은 "초 분 시 일 월 요일" 순서
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void updatePopularity() {
        List<Community> communities = communityRepository.findAll();
        log.info("[Popularity Batch] 시작 - 대상: {}건", communities.size());

        for (Community community : communities) {
            Long pinId = community.getPin().getPinId();

            // Pin에 달린 각 지표 수집
            int viewCount    = community.getPin().getViewCount();   // Pin 엔티티에 캐싱된 조회수
            int likeCount    = community.getPin().getLikeCount();   // Pin 엔티티에 캐싱된 공감수
            // 이모지·댓글은 별도 테이블이라 count 쿼리로 집계
            long emojiCount   = pinEmojiRepository.countActiveByPinId(pinId);
            long commentCount = commentRepository.countByPin_PinId(pinId);

            double score = calc(viewCount, likeCount, emojiCount, commentCount);

            community.updatePopularity(score);
        }

        log.info("[Popularity Batch] 완료");
    }

    // 인기도 계산 공식
    // popularity = (조회수 * 0.1) + (공감 * 0.4) + (이모지 * 0.2) + (댓글 * 0.3)
    private double calc(int viewCount, int likeCount, long emojiCount, long commentCount) {
        return (viewCount * VIEW_WEIGHT)
                + (likeCount * LIKE_WEIGHT)
                + (emojiCount * EMOJI_WEIGHT)
                + (commentCount * COMMENT_WEIGHT);
    }
}