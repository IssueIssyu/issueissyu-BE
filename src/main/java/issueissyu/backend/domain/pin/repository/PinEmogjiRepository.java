package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.mapping.PinEmogji;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PinEmogjiRepository extends JpaRepository<PinEmogji, Long> {

    // 핀 ID와 사용자 ID로 내 반응 1건 조회
    Optional<PinEmogji> findByPinPinIdAndUserUid(Long pinId, String uid);

    // 핀의 이모지 반응을 DB에서 바로 집계한다 (emoji_id 기준 GROUP BY)
    @Query("""
            SELECT pe.emogji.emojiId AS emogjiId,
                   pe.emogji.emojiImageUrl AS emojiImageUrl,
                   COUNT(pe) AS count
            FROM PinEmogji pe
            WHERE pe.pin.pinId = :pinId
            GROUP BY pe.emogji.emojiId, pe.emogji.emojiImageUrl
            """)
    List<PinEmojiCountProjection> countByPinIdGroupByEmoji(@Param("pinId") Long pinId);

    interface PinEmojiCountProjection {
        Long getEmogjiId();

        String getEmojiImageUrl();

        long getCount();
    }
}
