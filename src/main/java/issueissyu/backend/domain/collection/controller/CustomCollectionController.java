package issueissyu.backend.domain.collection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.collection.dto.req.CollectionBookmarkUpdateReqDTO;
import issueissyu.backend.domain.collection.dto.res.CollectionBookmarkUpdateResDTO;
import issueissyu.backend.domain.collection.dto.res.MyCollectionsResDTO;
import issueissyu.backend.domain.collection.dto.res.ProfileCollectionUpdateResDTO;
import issueissyu.backend.domain.collection.exception.code.CustomCollectionSuccessCode;
import issueissyu.backend.domain.collection.service.command.UserCustomCollectionCommandService;
import issueissyu.backend.domain.collection.service.query.UserCustomCollectionQueryService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/collections")
@RequiredArgsConstructor
@Validated
@Tag(name = "User collections", description = "마이페이지·컬렉션 커스텀 이미지")
public class CustomCollectionController {

    private final UserCustomCollectionQueryService userCustomCollectionQueryService;
    private final UserCustomCollectionCommandService userCustomCollectionCommandService;

    @Operation(
            summary = "마이페이지 프로필, 내 컬렉션 목록",
            description =
                    " 사용자의 프로필 정보와 컬렉션 목록을 반환 합니다. 마이페이지에서는 collections 중 isLocked == false 만 사용할 수 있습니다. myCollection 은 대표 프로필 컬렉션 요약입니다.")
    @GetMapping
    public ApiResponse<MyCollectionsResDTO> getMyCollections(@AuthenticationPrincipal String uid) {
        MyCollectionsResDTO result = userCustomCollectionQueryService.getMyCollections(uid);
        return ApiResponse.onSuccess(CustomCollectionSuccessCode.USER_COLLECTIONS_GET_SUCCESS, result);
    }

    @Operation(
            summary = "프로필 컬렉션 설정(갱신)",
            description = "대표 프로필 이미지로 쓸 컬렉션을 지정합니다. 항상 하나만 활성화되며 '끄기'는 없습니다(다른 컬렉션으로만 교체).")
    @PatchMapping("/{collectionId}/profile")
    public ApiResponse<ProfileCollectionUpdateResDTO> updateProfileCollection(
            @AuthenticationPrincipal String uid, @PathVariable Long collectionId) {
        ProfileCollectionUpdateResDTO result =
                userCustomCollectionCommandService.setProfileCollection(uid, collectionId);
        return ApiResponse.onSuccess(CustomCollectionSuccessCode.PROFILE_COLLECTION_UPDATE_SUCCESS, result);
    }

    @Operation(
            summary = "컬렉션 북마크 설정",
            description = "북마크는 최대 하나만 켜집니다. true이면 다른 해금 컬렉션의 북마크는 모두 해제된 뒤 이 컬렉션만 북마크됩니다.")
    @PatchMapping("/{collectionId}/bookmark")
    public ApiResponse<CollectionBookmarkUpdateResDTO> updateBookmark(
            @AuthenticationPrincipal String uid,
            @PathVariable Long collectionId,
            @Valid @RequestBody CollectionBookmarkUpdateReqDTO body) {
        CollectionBookmarkUpdateResDTO result =
                userCustomCollectionCommandService.setBookmark(uid, collectionId, body.getIsBookmarked());
        return ApiResponse.onSuccess(CustomCollectionSuccessCode.CUSTOM_COLLECTION_BOOKMARK_UPDATE_SUCCESS, result);
    }
}
