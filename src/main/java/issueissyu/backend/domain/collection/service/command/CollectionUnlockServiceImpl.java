package issueissyu.backend.domain.collection.service.command;

import issueissyu.backend.domain.collection.dto.res.NewlyUnlockedCollectionResDTO;
import issueissyu.backend.domain.collection.entity.CustomCollection;
import issueissyu.backend.domain.collection.entity.mapping.UserCustomCollection;
import issueissyu.backend.domain.collection.repository.CustomCollectionRepository;
import issueissyu.backend.domain.collection.repository.UserCustomCollectionRepository;
import issueissyu.backend.domain.pin.repository.PinLikeRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollectionUnlockServiceImpl implements CollectionUnlockService {

    private static final long LIKE_MISSION_COLLECTION_ID = 10L;
    private static final int REQUIRED_PIN_LIKE_COUNT = 3;

    private final UserRepository userRepository;
    private final CustomCollectionRepository customCollectionRepository;
    private final UserCustomCollectionRepository userCustomCollectionRepository;
    private final PinLikeRepository pinLikeRepository;

    @Override
    @Transactional
    public List<NewlyUnlockedCollectionResDTO> evaluateAndUnlockMissions(String uid) {
        List<NewlyUnlockedCollectionResDTO> newlyUnlocked = new ArrayList<>();
        unlockLikeMissionIfEligible(uid, newlyUnlocked);
        return newlyUnlocked;
    }

    private void unlockLikeMissionIfEligible(String uid, List<NewlyUnlockedCollectionResDTO> newlyUnlocked) {
        
        // 해금 되어있으면 패스
        if (userCustomCollectionRepository
                .findByUser_UidAndCustomCollection_CustomCollectionId(uid, LIKE_MISSION_COLLECTION_ID)
                .isPresent()) {
            return;
        }

        // 조건 확인
        if (pinLikeRepository.countByUser_Uid(uid) < REQUIRED_PIN_LIKE_COUNT) {
            return;
        }

        // 컬렉션이 있는지..
        CustomCollection collection = customCollectionRepository
                .findById(LIKE_MISSION_COLLECTION_ID)
                .orElse(null);
        if (collection == null) {
            return;
        }

        // 유저 조회
        User user = userRepository
                .findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        // 컬렉션 해금
        userCustomCollectionRepository.save(
                UserCustomCollection.builder().user(user).customCollection(collection).build());

        newlyUnlocked.add(
                NewlyUnlockedCollectionResDTO.builder()
                        .collectionId(collection.getCustomCollectionId())
                        .name(collection.getCustomCollectionName())
                        .imageUrl(collection.getCustomCollectionS3Url())
                        .unlockCondition(collection.getLockCondition())
                        .build());
    }
}
