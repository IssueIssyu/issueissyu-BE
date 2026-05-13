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

    @Override
    @Transactional
    public ProfileCollectionUpdateResDTO setProfileCollection(String uid, Long collectionId) {
        userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
        CustomCollection master =
                customCollectionRepository
                        .findById(collectionId)
                        .orElseThrow(() -> CustomCollectionException.of(CustomCollectionErrorCode.CUSTOM_COLLECTION_NOT_FOUND));

        UserCustomCollection mapping =
                userCustomCollectionRepository
                        .findByUser_UidAndCustomCollection_CustomCollectionId(uid, collectionId)
                        .orElseThrow(() -> CustomCollectionException.of(CustomCollectionErrorCode.CUSTOM_COLLECTION_LOCKED));

        List<UserCustomCollection> owned =
                userCustomCollectionRepository.findAllByUser_UidOrderByCustomCollection_CustomCollectionIdAsc(uid);
        owned.forEach(o -> o.setProfile(false));
        mapping.markAsProfile();

        return ProfileCollectionUpdateResDTO.builder()
                .uid(uid)
                .profileCollectionId(master.getCustomCollectionId())
                .profileImageUrl(master.getCustomCollectionS3Url())
                .build();
    }

    @Override
    @Transactional
    public CollectionBookmarkUpdateResDTO setBookmark(String uid, Long collectionId, boolean isBookmarked) {
        userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
        customCollectionRepository
                .findById(collectionId)
                .orElseThrow(() -> CustomCollectionException.of(CustomCollectionErrorCode.CUSTOM_COLLECTION_NOT_FOUND));

        UserCustomCollection mapping =
                userCustomCollectionRepository
                        .findByUser_UidAndCustomCollection_CustomCollectionId(uid, collectionId)
                        .orElseThrow(() -> CustomCollectionException.of(CustomCollectionErrorCode.CUSTOM_COLLECTION_LOCKED));

        mapping.setBookmark(isBookmarked);
        return CollectionBookmarkUpdateResDTO.builder()
                .customCollectionId(collectionId)
                .isBookmarked(isBookmarked)
                .build();
    }
}
