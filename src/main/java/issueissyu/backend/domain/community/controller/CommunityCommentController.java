package issueissyu.backend.domain.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;
import issueissyu.backend.domain.community.exception.code.CommunitySuccessCode;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.pin.dto.req.CommentReqDTO;
import issueissyu.backend.domain.pin.dto.res.CommentResDTO;
import issueissyu.backend.domain.pin.service.command.CommentCommandService;
import issueissyu.backend.domain.pin.service.query.CommentQueryService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Community Comment", description = "커뮤니티 댓글 API (핀 댓글 로직 공유)")
@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityCommentController {
    private static final Long TODO_PIN_ID_PLACEHOLDER = -1L;

    private final CommunityRepository communityRepository;
    private final CommentQueryService commentQueryService;
    private final CommentCommandService commentCommandService;

    @Operation(summary = "커뮤니티 댓글 목록 조회")
    @GetMapping("/{communityId}/comments")
    public ApiResponse<List<CommentResDTO>> getComments(
            @PathVariable Long communityId,
            @AuthenticationPrincipal String uid) {
        Long pinId = resolvePinId(communityId);
        return ApiResponse.onSuccess(
                CommunitySuccessCode.COMMUNITY_COMMENTS_200,
                commentQueryService.getComments(pinId, uid));
    }

    @Operation(summary = "커뮤니티 댓글 작성")
    @PostMapping("/{communityId}/comments")
    public ApiResponse<CommentResDTO> createComment(
            @PathVariable Long communityId,
            @AuthenticationPrincipal String uid,
            @Valid @RequestBody CommentReqDTO request) {
        Long pinId = resolvePinId(communityId);
        return ApiResponse.onSuccess(
                CommunitySuccessCode.COMMUNITY_CREATE_COMMENT_200,
                commentCommandService.createComment(pinId, uid, request));
    }

    @Operation(summary = "커뮤니티 댓글 수정", description = "댓글 ID만으로 수정합니다. (연결된 핀의 댓글과 동일 엔티티)")
    @PatchMapping("/comments/{commentId}")
    public ApiResponse<CommentResDTO> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal String uid,
            @Valid @RequestBody CommentReqDTO request) {
        // TODO: CommentCommandService가 commentId 전용 API를 제공하면 이 더미 pinId 전달을 제거한다.
        return ApiResponse.onSuccess(
                CommunitySuccessCode.COMMUNITY_UPDATE_COMMENT_200,
                commentCommandService.updateComment(TODO_PIN_ID_PLACEHOLDER, commentId, uid, request));
    }

    @Operation(summary = "커뮤니티 댓글 삭제", description = "댓글 ID만으로 삭제합니다.")
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal String uid) {
        // TODO: CommentCommandService가 commentId 전용 API를 제공하면 이 더미 pinId 전달을 제거한다.
        commentCommandService.deleteComment(TODO_PIN_ID_PLACEHOLDER, commentId, uid);
        return ApiResponse.onSuccess(CommunitySuccessCode.COMMUNITY_DELETE_COMMENT_200, null);
    }

    private Long resolvePinId(Long communityId) {
        return communityRepository.findDetailById(communityId)
                .orElseThrow(() -> CommunityException.of(CommunityErrorCode.COMMUNITY_404_1))
                .getPin()
                .getPinId();
    }
}
