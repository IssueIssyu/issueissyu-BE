package issueissyu.backend.domain.location.service.query;

import issueissyu.backend.domain.location.dto.res.LocationRegionItemResDTO;
import issueissyu.backend.domain.location.dto.res.LocationRegionListResDTO;
import issueissyu.backend.domain.location.dto.res.UserRegionSnippetResDTO;
import issueissyu.backend.domain.location.entity.Location;
import issueissyu.backend.domain.location.repository.LocationRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.entity.UserLocation;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationRegionListQueryServiceImpl implements LocationRegionListQueryService {

    private static final Set<Long> EXCLUDED_LOCATION_IDS =
            Set.of(
                    1L, 27L, 44L, 54L, 65L, 71L, 77L, 84L, 85L, 90L, 95L, 98L, 105L, 108L, 120L, 136L,
                    137L, 152L, 153L, 170L, 193L, 194L, 218L, 219L, 242L, 245L, 264L, 265L);

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    @Override
    public LocationRegionListResDTO getRegionList(String uid) {

        User user = userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        List<LocationRegionItemResDTO> locations =
                locationRepository.findAllExcludingIds(EXCLUDED_LOCATION_IDS).stream()
                        .map(this::toItem)
                        .collect(Collectors.toList());

        UserRegionSnippetResDTO userSnippet = resolveUserSnippet(user);
        return new LocationRegionListResDTO(userSnippet, locations);
    }

    private LocationRegionItemResDTO toItem(Location loc) {
        return new LocationRegionItemResDTO(loc.getLocationId(), shortRegionName(loc.getRegion()));
    }

    private UserRegionSnippetResDTO resolveUserSnippet(User user) {
        UserLocation ul = user.getUserLocation();
        if (ul == null || ul.getUserPoint() == null || ul.getLocation() == null) {
            return null;
        }
        Location loc = ul.getLocation();
        return new UserRegionSnippetResDTO(loc.getLocationId(), shortRegionName(loc.getRegion()));
    }

    // {@code location} 컬럼 값을 공백으로 나눈 뒤 마지막 토큰을 지역구 표시명으로 사용합니다.
    static String shortRegionName(String fullRegion) {
        if (fullRegion == null || fullRegion.isBlank()) {
            return "";
        }
        String[] parts = fullRegion.trim().split("\\s+");
        return parts[parts.length - 1];
    }
}
