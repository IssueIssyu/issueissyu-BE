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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pin Comment", description = "핀 댓글 API")
@RestController
@RequestMapping("/api/pins/{pinId}/comments")
@RequiredArgsConstructor
public class PinCommentController {
    private final CommentQueryService commentQueryService;
    private final CommentCommandService commentCommandService;

    @Operation(summary = "핀 댓글 목록 조회")
    @GetMapping
    public ApiResponse<Page<CommentResDTO>> getComments(
            @PathVariable Long pinId,
            @AuthenticationPrincipal String uid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.onSuccess(
                PinSuccessCode.PIN_COMMENTS_200,
                commentQueryService.getComments(pinId, uid, pageable)
        );
    }

    @Operation(summary = "핀 댓글 작성")
    @PostMapping
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
    @PatchMapping("/{commentId}")
    public ApiResponse<CommentResDTO> updateComment(
            @PathVariable Long pinId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal String uid,
            @Valid @RequestBody CommentReqDTO request
    ) {
        return ApiResponse.onSuccess(
                PinSuccessCode.UPDATE_COMMENT_200,
                commentCommandService.updateComment(pinId, commentId, uid, request)
        );
    }

    @Operation(summary = "핀 댓글 삭제")
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long pinId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal String uid
    ) {
        commentCommandService.deleteComment(pinId, commentId, uid);
        return ApiResponse.onSuccess(PinSuccessCode.DELETE_COMMENT_200, null);
    }
}
