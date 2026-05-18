package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.mapping.PinEmoji;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PinEmojiRepository extends JpaRepository<PinEmoji, Long> {

    Optional<PinEmoji> findByPinPinIdAndUserUidAndEmojiEmojiId(Long pinId, String uid, Long emojiId);

    Optional<PinEmoji> findByPinPinIdAndUserUidAndActiveTrue(Long pinId, String uid);

    // 같은 pin/uid 처리 경쟁 시 현재 선택 row를 잠가 active 중복 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE) // 비관적 락 사용
    @Query("""
            SELECT pe
            FROM PinEmoji pe
            WHERE pe.pin.pinId = :pinId
              AND pe.user.uid = :uid
              AND pe.active = true
            """)
    Optional<PinEmoji> findActiveByPinIdAndUidForUpdate(@Param("pinId") Long pinId, @Param("uid") String uid);

    @Query("""
            SELECT pe.emoji.emojiId AS emojiId,
                   pe.emoji.emojiImageUrl AS emojiImageUrl,
                   COUNT(pe) AS count
            FROM PinEmoji pe
            WHERE pe.pin.pinId = :pinId
              AND pe.active = true
            GROUP BY pe.emoji.emojiId, pe.emoji.emojiImageUrl
            """)
    List<PinEmojiCountProjection> countByPinIdGroupByEmoji(@Param("pinId") Long pinId);

    // 핀 이모지 집계 쿼리 결과 담기용
    interface PinEmojiCountProjection {
        Long getEmojiId();
        String getEmojiImageUrl();
        long getCount();
    }

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PinEmoji pe WHERE pe.pin.pinId = :pinId")
    void deleteByPin_PinId(@Param("pinId") Long pinId);

    // 배치에서 핀 하나의 활성 이모지 총 개수를 셀 때 사용
    @Query("""
        SELECT COUNT(pe)
        FROM PinEmoji pe
        WHERE pe.pin.pinId = :pinId
          AND pe.active = true
        """)
    long countActiveByPinId(@Param("pinId") Long pinId);
}
