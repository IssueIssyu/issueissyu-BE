package issueissyu.backend.domain.location.dto.res;

import issueissyu.backend.domain.location.entity.Location;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLocationCertResDto {
    private String address;

    public static UserLocationCertResDto from(Location location) {
        return new UserLocationCertResDto(
                location.getAdmCode()
        );
    }

}
