package issueissyu.backend.domain.collection.service.command;

import issueissyu.backend.domain.collection.dto.res.CollectionBookmarkUpdateResDTO;
import issueissyu.backend.domain.collection.dto.res.ProfileCollectionUpdateResDTO;
import issueissyu.backend.domain.collection.entity.CustomCollection;
import issueissyu.backend.domain.collection.entity.mapping.UserCustomCollection;
import issueissyu.backend.domain.collection.exception.CustomCollectionException;
import issueissyu.backend.domain.collection.exception.code.CustomCollectionErrorCode;
import issueissyu.backend.domain.collection.repository.CustomCollectionRepository;
import issueissyu.backend.domain.collection.repository.UserCustomCollectionRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCustomCollectionCommandServiceImpl implements UserCustomCollectionCommandService {

    private final UserRepository userRepository;
    private final CustomCollectionRepository customCollectionRepository;
    private final UserCustomCollectionRepository userCustomCollectionRepository;

    // 해금된 컬렉션만 프로필로 설정 가능
    @Override
    @Transactional
    public ProfileCollectionUpdateResDTO setProfileCollection(String uid, Long collectionId) {
        // 사용자 검증
        userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        // 컬렉션 검증
        CustomCollection catalogDefinition =
                customCollectionRepository
                        .findById(collectionId)
                        .orElseThrow(() -> CustomCollectionException.of(CustomCollectionErrorCode.CUSTOM_COLLECTION_NOT_FOUND));

        List<UserCustomCollection> allUserUnlockRows =
                userCustomCollectionRepository.findAllByUser_UidOrderByCustomCollection_CustomCollectionIdAsc(uid);

        UserCustomCollection userUnlockRow = allUserUnlockRows.stream()
                .filter(row -> row.getCustomCollection().getCustomCollectionId().equals(collectionId))
                .findFirst()
                .orElseThrow(() -> CustomCollectionException.of(CustomCollectionErrorCode.CUSTOM_COLLECTION_LOCKED));

        allUserUnlockRows.forEach(row -> row.setProfile(false));
        userUnlockRow.markAsProfile();

        return ProfileCollectionUpdateResDTO.builder()
                .profileCollectionId(catalogDefinition.getCustomCollectionId())
                .profileImageUrl(catalogDefinition.getCustomCollectionS3Url())
                .build();
    }

    // 북마크는 하나만 켜짐, 0 도 가능
    @Override
    @Transactional
    public CollectionBookmarkUpdateResDTO setBookmark(String uid, Long collectionId, boolean isBookmarked) {
        
        // 사용자 검증
        userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        // 컬렉션 검증
        customCollectionRepository
                .findById(collectionId)
                .orElseThrow(() -> CustomCollectionException.of(CustomCollectionErrorCode.CUSTOM_COLLECTION_NOT_FOUND));

        if (isBookmarked) {
            List<UserCustomCollection> allUserUnlockRows =
                    userCustomCollectionRepository.findAllByUser_UidOrderByCustomCollection_CustomCollectionIdAsc(uid);
            UserCustomCollection userUnlockRow = allUserUnlockRows.stream()
                    .filter(row -> row.getCustomCollection().getCustomCollectionId().equals(collectionId))
                    .findFirst()
                    .orElseThrow(() -> CustomCollectionException.of(CustomCollectionErrorCode.CUSTOM_COLLECTION_LOCKED));
            allUserUnlockRows.forEach(row -> row.setBookmark(false));
            userUnlockRow.setBookmark(true);
        } else {
            userCustomCollectionRepository
                    .findByUser_UidAndCustomCollection_CustomCollectionId(uid, collectionId)
                    .orElseThrow(() -> CustomCollectionException.of(CustomCollectionErrorCode.CUSTOM_COLLECTION_LOCKED))
                    .setBookmark(false);
        }

        return CollectionBookmarkUpdateResDTO.builder()
                .customCollectionId(collectionId)
                .isBookmarked(isBookmarked)
                .build();
    }
}
