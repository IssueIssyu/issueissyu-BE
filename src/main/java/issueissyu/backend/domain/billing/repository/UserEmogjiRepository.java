package issueissyu.backend.domain.billing.repository;

import issueissyu.backend.domain.pin.entity.mapping.UserEmogji;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserEmogjiRepository extends JpaRepository<UserEmogji, Long> {

    boolean existsByUserUidAndEmogjiEmojiId(String uid, Long emojiId);
    boolean existsByPurchaseToken(String purchaseToken);
    Optional<UserEmogji> findByUserUidAndEmogjiEmojiId(String uid, Long emojiId);

    @Query("SELECT ue.emogji.emojiId FROM UserEmogji ue WHERE ue.user.uid = :uid")
    List<Long> findOwnedEmojiIdsByUid(@Param("uid") String uid);
}
