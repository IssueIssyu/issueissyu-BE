package issueissyu.backend.domain.community.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommunitySuccessCode implements BaseSuccessCode {
    COMMUNITY_FEED_200(HttpStatus.OK, "COMMUNITY_FEED_200", "커뮤니티 피드 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
