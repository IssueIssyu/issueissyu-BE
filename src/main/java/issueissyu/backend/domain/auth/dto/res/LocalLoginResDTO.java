package issueissyu.backend.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

// 로컬 로그인 성공 응답 DTO
// isNew=true  → user.tempUuid 반환 (온보딩 전 임시 아이디)
// isNew=false → user.uuid 반환 (확정 아이디)
@Getter
@Builder
public class LocalLoginResDTO {

    private String accessToken;
    private String refreshToken;

    // 액세스 토큰 만료 시간 (초 단위)
    private long expiresIn;

    // 첫 로그인 여부 (true: 신규 회원 → 온보딩, false: 기존 회원)
    @JsonProperty("isNew")
    private boolean isNew;

    @JsonProperty("social_type")
    private String socialType;

    private UserInfo user;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        private String uuid;

        @JsonProperty("temp_uuid")
        private String tempUuid;

        private String userName;
    }
}
