package issueissyu.backend.domain.community.repository;

import issueissyu.backend.domain.community.entity.CardnewsImageS3;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardnewsImageS3Repository extends JpaRepository<CardnewsImageS3, Long> {

    void deleteByCommunity_CommunityId(Long communityId);
}
