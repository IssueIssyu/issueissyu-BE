package issueissyu.backend.domain.collection.service.query;

import issueissyu.backend.domain.collection.dto.res.MyCollectionsResDTO;
import issueissyu.backend.domain.collection.dto.res.ProfileCollectionSummaryResDTO;
import issueissyu.backend.domain.collection.dto.res.UserCollectionItemResDTO;
import issueissyu.backend.domain.collection.entity.CustomCollection;
import issueissyu.backend.domain.collection.entity.mapping.UserCustomCollection;
import issueissyu.backend.domain.collection.repository.CustomCollectionRepository;
import issueissyu.backend.domain.collection.repository.UserCustomCollectionRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCustomCollectionQueryServiceImpl implements UserCustomCollectionQueryService {

    private final UserRepository userRepository;
    private final CustomCollectionRepository customCollectionRepository;
    private final UserCustomCollectionRepository userCustomCollectionRepository;

    @Override
    public MyCollectionsResDTO getMyCollections(String uid) {
        User user = userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        List<CustomCollection> catalog = customCollectionRepository.findAllByOrderByCustomCollectionIdAsc();
        // user_custom_collection 행이 있으면 해당 컬렉션은 해금된 상태로 본다.
        Map<Long, UserCustomCollection> unlockedMappingByCollectionId =
                userCustomCollectionRepository
                        .findAllByUser_UidOrderByCustomCollection_CustomCollectionIdAsc(uid)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        u -> u.getCustomCollection().getCustomCollectionId(),
                                        Function.identity()));

        ProfileCollectionSummaryResDTO profileSummary =
                unlockedMappingByCollectionId.values().stream()
                        .filter(UserCustomCollection::isProfile)
                        .findFirst()
                        .map(u -> toProfileSummary(u.getCustomCollection()))
                        .orElse(null);

        List<UserCollectionItemResDTO> collections =
                catalog.stream()
                        .map(
                                definition ->
                                        toCollectionItem(
                                                definition,
                                                unlockedMappingByCollectionId.get(
                                                        definition.getCustomCollectionId())))
                        .toList();

        return MyCollectionsResDTO.builder()
                .nickname(user.getNickname())
                .profileCollection(profileSummary)
                .collections(collections)
                .build();
    }

    private static ProfileCollectionSummaryResDTO toProfileSummary(CustomCollection c) {
        return ProfileCollectionSummaryResDTO.builder()
                .collectionId(c.getCustomCollectionId())
                .name(c.getCustomCollectionName())
                .imageUrl(c.getCustomCollectionS3Url())
                .build();
    }

    /**
     * @param definition 마스터 컬렉션(카탈로그)
     * @param unlockRow 해당 유저의 해금 매핑 행. 없으면(null) 아직 잠금.
     */
    private static UserCollectionItemResDTO toCollectionItem(
            CustomCollection definition, UserCustomCollection unlockRow) {
        if (unlockRow != null) {
            return UserCollectionItemResDTO.builder()
                    .collectionId(definition.getCustomCollectionId())
                    .name(definition.getCustomCollectionName())
                    .imageUrl(definition.getCustomCollectionS3Url())
                    .isLocked(false)
                    .isBookmarked(unlockRow.isBookmark())
                    .isProfile(unlockRow.isProfile())
                    .unlockCondition("")
                    .build();
        }
        return UserCollectionItemResDTO.builder()
                .collectionId(definition.getCustomCollectionId())
                .name(definition.getCustomCollectionName())
                .imageUrl(definition.getCustomCollectionS3Url())
                .isLocked(true)
                .isBookmarked(false)
                .isProfile(false)
                .unlockCondition(definition.getLockCondition())
                .build();
    }
}
