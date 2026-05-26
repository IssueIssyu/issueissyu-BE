package issueissyu.backend.domain.collection.service.query;

import issueissyu.backend.domain.collection.dto.res.MyCollectionsResDTO;
import issueissyu.backend.domain.collection.dto.res.NewlyUnlockedCollectionResDTO;
import issueissyu.backend.domain.collection.dto.res.ProfileCollectionSummaryResDTO;
import issueissyu.backend.domain.collection.dto.res.UserCollectionItemResDTO;
import issueissyu.backend.domain.collection.entity.CustomCollection;
import issueissyu.backend.domain.collection.entity.mapping.UserCustomCollection;
import issueissyu.backend.domain.collection.repository.CustomCollectionRepository;
import issueissyu.backend.domain.collection.repository.UserCustomCollectionRepository;
import issueissyu.backend.domain.collection.service.command.CollectionUnlockService;
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
    private final CollectionUnlockService collectionUnlockService;

    // 마스터 전부 ID 순으로 내려주고, 해금 행 있으면 북마크 반영 / 프로필 요약은 myCollection
    @Override
    @Transactional(readOnly = false)
    public MyCollectionsResDTO getMyCollections(String uid, boolean checkUnlock) {
        User user = userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        List<NewlyUnlockedCollectionResDTO> newlyUnlocked =
                checkUnlock ? collectionUnlockService.evaluateAndUnlockMissions(uid) : List.of();

        List<CustomCollection> catalogDefinitions =
                customCollectionRepository.findAllByOrderByCustomCollectionIdAsc();

        Map<Long, UserCustomCollection> unlockRowByCatalogId =
                userCustomCollectionRepository
                        .findAllByUser_UidOrderByCustomCollection_CustomCollectionIdAsc(uid)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        row -> row.getCustomCollection().getCustomCollectionId(),
                                        Function.identity()));

        ProfileCollectionSummaryResDTO profileSummary =
                unlockRowByCatalogId.values().stream()
                        .filter(UserCustomCollection::isProfile)
                        .findFirst()
                        .map(row -> toProfileSummary(row.getCustomCollection()))
                        .orElse(null);

        List<UserCollectionItemResDTO> collections =
                catalogDefinitions.stream()
                        .map(
                                definition ->
                                        toCollectionItem(
                                                definition,
                                                unlockRowByCatalogId.get(definition.getCustomCollectionId())))
                        .toList();

        return MyCollectionsResDTO.builder()
                .nickname(user.getNickname())
                .myCollection(profileSummary)
                .collections(collections)
                .newlyUnlocked(newlyUnlocked)
                .build();
    }

    private static ProfileCollectionSummaryResDTO toProfileSummary(CustomCollection catalogDefinition) {
        return ProfileCollectionSummaryResDTO.builder()
                .collectionId(catalogDefinition.getCustomCollectionId())
                .name(catalogDefinition.getCustomCollectionName())
                .imageUrl(catalogDefinition.getCustomCollectionS3Url())
                .build();
    }

    // userUnlockRow 없으면 잠금 처리
    private static UserCollectionItemResDTO toCollectionItem(
            CustomCollection catalogDefinition, UserCustomCollection userUnlockRow) {
        if (userUnlockRow != null) {
            return UserCollectionItemResDTO.builder()
                    .collectionId(catalogDefinition.getCustomCollectionId())
                    .name(catalogDefinition.getCustomCollectionName())
                    .imageUrl(catalogDefinition.getCustomCollectionS3Url())
                    .isLocked(false)
                    .isBookmarked(userUnlockRow.isBookmark())
                    .unlockCondition("")
                    .build();
        }
        return UserCollectionItemResDTO.builder()
                .collectionId(catalogDefinition.getCustomCollectionId())
                .name(catalogDefinition.getCustomCollectionName())
                .imageUrl(catalogDefinition.getCustomCollectionS3Url())
                .isLocked(true)
                .isBookmarked(false)
                .unlockCondition(catalogDefinition.getLockCondition())
                .build();
    }
}
