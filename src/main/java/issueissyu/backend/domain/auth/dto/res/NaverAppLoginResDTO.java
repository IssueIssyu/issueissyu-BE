package issueissyu.backend.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

// 네이버 앱 로그인 성공 응답 DTO
// isNew=true  → user.tempUuid 반환 (온보딩 전 임시 아이디)
// isNew=false → user.uuid 반환 (확정 아이디)
@Getter
@Builder
public class NaverAppLoginResDTO {

    private String accessToken;
    private String refreshToken;

    // 액세스 토큰 만료 시간 (초 단위)
    private long expiresIn;

    // 첫 로그인 여부 (true: 신규 회원 → 온보딩, false: 기존 회원)
    // 필드명 isNew + Lombok getter 시 Jackson이 "new"까지 중복 직렬화함 → newUser로 보관
    @Getter(AccessLevel.NONE)
    private boolean newUser;

    @JsonProperty("isNew")
    public boolean isNew() {
        return newUser;
    }

    @JsonProperty("socialType")
    private String socialType;

    private UserInfo user;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {

        // 기존 회원: 확정 uuid
        private String uuid;

        // 신규 회원: 온보딩 완료 전 임시 uuid
        @JsonProperty("tempUuid")
        private String tempUuid;

        @JsonProperty("userName")
        private String userName;
    }
}
