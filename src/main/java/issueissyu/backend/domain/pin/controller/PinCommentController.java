package issueissyu.backend.domain.pin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.pin.dto.req.CommentReqDTO;
import issueissyu.backend.domain.pin.dto.res.CommentResDTO;
import issueissyu.backend.domain.pin.exception.code.PinSuccessCode;
import issueissyu.backend.domain.pin.service.command.CommentCommandService;
import issueissyu.backend.domain.pin.service.query.CommentQueryService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Pin Comment", description = "핀 댓글 API")
@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
public class PinCommentController {
    private final CommentQueryService commentQueryService;
    private final CommentCommandService commentCommandService;

    @Operation(summary = "핀 댓글 목록 조회", description = "특정 핀 댓글을 최신순으로 정렬하여 제공합니다.")
    @GetMapping("/{pinId}/comments")
    public ApiResponse<List<CommentResDTO>> getComments(
            @PathVariable Long pinId,
            @AuthenticationPrincipal String uid
    ) {
        return ApiResponse.onSuccess(
                PinSuccessCode.PIN_COMMENTS_200,
                commentQueryService.getComments(pinId, uid)
        );
    }

    @Operation(summary = "핀 댓글 작성")
    @PostMapping("/{pinId}/comments")
    public ApiResponse<CommentResDTO> createComment(
            @PathVariable Long pinId,
            @AuthenticationPrincipal String uid,
            @Valid @RequestBody CommentReqDTO request
    ) {
        return ApiResponse.onSuccess(
                PinSuccessCode.CREATE_COMMENT_200,
                commentCommandService.createComment(pinId, uid, request)
        );
    }

    @Operation(summary = "핀 댓글 수정")
    @PatchMapping("/comments/{commentId}")
    public ApiResponse<CommentResDTO> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal String uid,
            @Valid @RequestBody CommentReqDTO request
    ) {
        return ApiResponse.onSuccess(
                PinSuccessCode.UPDATE_COMMENT_200,
                commentCommandService.updateComment(commentId, uid, request)
        );
    }

    @Operation(summary = "핀 댓글 삭제")
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal String uid
    ) {
        commentCommandService.deleteComment(commentId, uid);
        return ApiResponse.onSuccess(PinSuccessCode.DELETE_COMMENT_200, null);
    }
}
