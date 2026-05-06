package issueissyu.backend.domain.community.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommunityErrorCode implements BaseErrorCode {
    COMMUNITY_400_1(HttpStatus.BAD_REQUEST, "COMMUNITY_400_1", "지원하지 않는 커뮤니티 탭입니다."),
    COMMUNITY_400_2(HttpStatus.BAD_REQUEST, "COMMUNITY_400_2", "지원하지 않는 지역 코드입니다."),
    COMMUNITY_400_3(HttpStatus.BAD_REQUEST, "COMMUNITY_400_3", "유효하지 않은 커서 형식입니다."),
    COMMUNITY_403_1(HttpStatus.FORBIDDEN, "COMMUNITY_403_1", "커뮤니티 게시물을 삭제할 권한이 없습니다."),
    COMMUNITY_404_1(HttpStatus.NOT_FOUND, "COMMUNITY_404_1", "존재하지 않는 커뮤니티 게시물입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
