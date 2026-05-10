package issueissyu.backend.domain.community.exception.code;

import issueissyu.backend.domain.community.enums.CommunityTab;
import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommunitySuccessCode implements BaseSuccessCode {
    COMMUNITY_FEED_ALL_200(HttpStatus.OK, "COMMUNITY_FEED_ALL_200", "전체 커뮤니티 목록 조회에 성공했습니다."),
    COMMUNITY_FEED_ISSUE_200(HttpStatus.OK, "COMMUNITY_FEED_ISSUE_200", "이슈 커뮤니티 목록 조회에 성공했습니다."),
    COMMUNITY_FEED_STORE_200(HttpStatus.OK, "COMMUNITY_FEED_STORE_200", "가게 홍보 커뮤니티 목록 조회에 성공했습니다."),
    COMMUNITY_FEED_COMMUNICATION_200(HttpStatus.OK, "COMMUNITY_FEED_COMMUNICATION_200", "소통 커뮤니티 목록 조회에 성공했습니다."),
    COMMUNITY_DETAIL_200(HttpStatus.OK, "COMMUNITY_DETAIL_200", "커뮤니티 게시물 상세 조회에 성공했습니다."),
    COMMUNITY_DELETE_200(HttpStatus.OK, "COMMUNITY_DELETE_200", "커뮤니티 게시물 삭제에 성공했습니다."),
    COMMUNITY_TAKEDOWN_200(HttpStatus.OK, "COMMUNITY_TAKEDOWN_200", "커뮤니티 게시물을 내렸습니다."),
    COMMUNITY_COMMENTS_200(HttpStatus.OK, "COMMUNITY_COMMENTS_200", "커뮤니티 댓글 목록 조회에 성공했습니다."),
    COMMUNITY_CREATE_COMMENT_200(HttpStatus.OK, "COMMUNITY_CREATE_COMMENT_200", "커뮤니티 댓글 작성에 성공했습니다."),
    COMMUNITY_UPDATE_COMMENT_200(HttpStatus.OK, "COMMUNITY_UPDATE_COMMENT_200", "커뮤니티 댓글 수정에 성공했습니다."),
    COMMUNITY_DELETE_COMMENT_200(HttpStatus.OK, "COMMUNITY_DELETE_COMMENT_200", "커뮤니티 댓글 삭제에 성공했습니다."),
    COMMUNITY_DECLARATION_200(HttpStatus.OK, "COMMUNITY_DECLARATION_200", "신고가 접수되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public static CommunitySuccessCode forTab(CommunityTab tab) {
        return switch (tab) {
            case ISSUE -> COMMUNITY_FEED_ISSUE_200;
            case STORE -> COMMUNITY_FEED_STORE_200;
            case COMMUNICATION -> COMMUNITY_FEED_COMMUNICATION_200;
            default -> COMMUNITY_FEED_ALL_200;
        };
    }
}
