package issueissyu.backend.domain.community.service.command;

import issueissyu.backend.domain.community.entity.CardnewsImageS3;
import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.enums.CommunityType;
import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;
import issueissyu.backend.domain.community.repository.CardnewsImageS3Repository;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityCommandServiceImpl implements CommunityCommandService {

    private final CommunityRepository communityRepository;
    private final CommunicationPinCleaner communicationPinCleaner;
    private final CardnewsImageS3Repository cardnewsImageS3Repository;
    private final S3Utils s3Utils;

    @Override
    public void deleteCommunity(Long communityId, String uid) {
        Community community = communityRepository.findDetailById(communityId)
                .orElseThrow(() -> CommunityException.of(CommunityErrorCode.COMMUNITY_404_1));

        // 소통 타입만 삭제 허용
        if (community.getCommunityType() != CommunityType.COMMUNICATION) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_403_2);
        }
        if (!community.getPin().getUser().getUid().equals(uid)) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_403_1);
        }

        communicationPinCleaner.deleteByPinId(community.getPin().getPinId());
    }

    @Override
    public void takedownCommunity(Long communityId, String uid) {
        Community community = communityRepository.findDetailById(communityId)
                .orElseThrow(() -> CommunityException.of(CommunityErrorCode.COMMUNITY_404_1));

        CommunityType type = community.getCommunityType();
        if (type != CommunityType.ISSUE && type != CommunityType.COMMUNICATION) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_403_4);
        }
        if (!community.getPin().getUser().getUid().equals(uid)) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_403_3);
        }

        // cardnews_image_s3는 Community에 cascade가 없으므로 명시적으로 먼저 삭제
        // findDetailById에서 left join fetch로 로드된 데이터를 활용
        List<String> cardnewsKeys = community.getCardnewsImages().stream()
                .map(CardnewsImageS3::getCardnewsImageS3Key)
                .toList();
        if (!cardnewsKeys.isEmpty()) {
            cardnewsImageS3Repository.deleteByCommunity_CommunityId(communityId);
        }

        communityRepository.delete(community);

        // DB 삭제 완료 후 S3 객체 삭제
        cardnewsKeys.forEach(s3Utils::deleteIfNotReserved);
    }
}
