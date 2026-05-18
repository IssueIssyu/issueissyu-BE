package issueissyu.backend.domain.location.service.query;

import issueissyu.backend.domain.location.dto.res.LocationRegionGroupResDTO;
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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationRegionListQueryServiceImpl implements LocationRegionListQueryService {

    private static final Long SEJONG_LOCATION_ID = 83L;

    private static final Collator KOREAN_REGION_COLLATOR = Collator.getInstance(Locale.KOREAN);

    private static final Comparator<LocationRegionItemResDTO> SUB_LOCATION_COLLATOR_ORDER =
            Comparator.comparing(
                    LocationRegionItemResDTO::location, Comparator.nullsLast(KOREAN_REGION_COLLATOR::compare));

    // location_id가 83인 행 전용 블록. 세종시가 너무 특별하다 젱장.
    private static final LocationRegionGroupResDTO SEJONG_GROUP_RES =
            new LocationRegionGroupResDTO(
                    "세종특별자치시",
                    List.of(new LocationRegionItemResDTO(SEJONG_LOCATION_ID, "세종특별자치시")));

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    @Override
    public LocationRegionListResDTO getRegionList(String uid) {

        User user = userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        List<LocationRegionGroupResDTO> locations = buildGroupedLocations(locationRepository.findAllByOrderByLocationIdAsc());

        UserRegionSnippetResDTO userSnippet = resolveUserSnippet(user);
        return new LocationRegionListResDTO(userSnippet, locations);
    }

    private List<LocationRegionGroupResDTO> buildGroupedLocations(List<Location> ordered) {
        List<LocationRegionGroupResDTO> groups = new ArrayList<>();
        String currentSuper = null;
        List<LocationRegionItemResDTO> subs = new ArrayList<>();

        for (Location loc : ordered) {
            if (loc.getLocationId().equals(SEJONG_LOCATION_ID)) {
                flushGroup(groups, currentSuper, subs);
                currentSuper = null;
                subs.clear();
                groups.add(SEJONG_GROUP_RES);
                continue;
            }

            String raw = loc.getRegion();
            if (raw == null || raw.isBlank()) {
                continue;
            }

            String[] parts = raw.trim().split("\\s+");
            if (parts.length == 0) {
                continue;
            }

            if (parts.length == 1) {
                flushGroup(groups, currentSuper, subs);
                currentSuper = parts[0];
                subs = new ArrayList<>();
                continue;
            }

            if (currentSuper == null) {
                continue;
            }

            subs.add(new LocationRegionItemResDTO(loc.getLocationId(), parts[parts.length - 1]));
        }

        flushGroup(groups, currentSuper, subs);
        return groups;
    }

    private static void flushGroup(
            List<LocationRegionGroupResDTO> groups, String superLocation, List<LocationRegionItemResDTO> subs) {
        if (superLocation != null && !subs.isEmpty()) {
            subs.sort(SUB_LOCATION_COLLATOR_ORDER);
            groups.add(new LocationRegionGroupResDTO(superLocation, List.copyOf(subs)));
        }
    }

    private UserRegionSnippetResDTO resolveUserSnippet(User user) {
        UserLocation ul = user.getUserLocation();
        if (ul == null || ul.getUserPoint() == null || ul.getLocation() == null) {
            return null;
        }
        Location loc = ul.getLocation();
        return new UserRegionSnippetResDTO(loc.getLocationId(), shortRegionName(loc.getRegion()));
    }

    // {@code location} 컬럼 값을 공백으로 나눈 뒤 마지막 토큰을 사용자 시군구 표시명으로 사용합니다.
    static String shortRegionName(String fullRegion) {
        if (fullRegion == null || fullRegion.isBlank()) {
            return "";
        }
        String[] parts = fullRegion.trim().split("\\s+");
        return parts[parts.length - 1];
    }
}
