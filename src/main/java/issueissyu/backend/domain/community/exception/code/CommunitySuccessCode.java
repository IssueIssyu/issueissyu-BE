package issueissyu.backend.domain.community.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommunitySuccessCode implements BaseSuccessCode {
    COMMUNITY_FEED_200(HttpStatus.OK, "COMMUNITY_FEED_200", "커뮤니티 피드 조회에 성공했습니다."),
    COMMUNITY_DETAIL_200(HttpStatus.OK, "COMMUNITY_DETAIL_200", "커뮤니티 게시물 상세 조회에 성공했습니다."),
    COMMUNITY_DELETE_200(HttpStatus.OK, "COMMUNITY_DELETE_200", "커뮤니티 게시물 삭제에 성공했습니다."),
    COMMUNITY_COMMENTS_200(HttpStatus.OK, "COMMUNITY_COMMENTS_200", "커뮤니티 댓글 목록 조회에 성공했습니다."),
    COMMUNITY_CREATE_COMMENT_200(HttpStatus.OK, "COMMUNITY_CREATE_COMMENT_200", "커뮤니티 댓글 작성에 성공했습니다."),
    COMMUNITY_UPDATE_COMMENT_200(HttpStatus.OK, "COMMUNITY_UPDATE_COMMENT_200", "커뮤니티 댓글 수정에 성공했습니다."),
    COMMUNITY_DELETE_COMMENT_200(HttpStatus.OK, "COMMUNITY_DELETE_COMMENT_200", "커뮤니티 댓글 삭제에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
