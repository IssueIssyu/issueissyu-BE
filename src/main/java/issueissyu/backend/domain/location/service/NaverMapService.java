package issueissyu.backend.domain.location.service;

import issueissyu.backend.domain.location.dto.res.NaverReverseGeocodeResDTO;
import issueissyu.backend.domain.location.exception.LocationException;
import issueissyu.backend.domain.location.exception.code.LocationErrorCode;
import issueissyu.backend.domain.location.service.command.NaverMapGeocodeService;
import issueissyu.backend.domain.location.service.command.NaverMapReverseGeocodeService;
import lombok.RequiredArgsConstructor;
import org.postgresql.geometric.PGpoint;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NaverMapService {
    private final NaverMapReverseGeocodeService naverMapReverseGeocodeService;
    private final NaverMapGeocodeService naverMapGeocodeService;


    public PGpoint geocodeToPoint(String address) {
        return naverMapGeocodeService.geocodeToPoint(address);
    }

    public NaverReverseGeocodeResDTO reverseGeocode(PGpoint point) {
        return naverMapReverseGeocodeService.reverseGeocode(point);
    }
    public PGpoint normalizePoint(PGpoint point) {
        String roadAddress = resolveRoadAddress(point);
        return geocodeToPoint(roadAddress);
    }
    // 두좌표가 같은 구인지 확인하는 서비스 메소드
    public boolean isSameSigungu(PGpoint firstPoint, PGpoint secondPoint) {
        String firstSigungu = resolveSigungu(firstPoint);
        String secondSigungu = resolveSigungu(secondPoint);
        return firstSigungu.equals(secondSigungu);
    }


    public String resolveRoadAddressOf(PGpoint point) {
        return resolveRoadAddress(point);
    }

    private String resolveRoadAddress(PGpoint point) {
        NaverReverseGeocodeResDTO result = naverMapReverseGeocodeService.reverseGeocode(point);
        return result.results().stream()
                .filter(item -> "roadaddr".equalsIgnoreCase(item.name()))
                .map(this::buildRoadAddress)
                .filter(address -> address != null && !address.isBlank())
                .findFirst()
                .or(() -> resolveJibunAddress(result))
                .or(() -> resolveRegionAddress(result))
                .orElseThrow(() -> LocationException.of(LocationErrorCode.LOCATION_ADDRESS_NOT_FOUND));
    }

    private String buildRoadAddress(NaverReverseGeocodeResDTO.ResultItem item) {
        NaverReverseGeocodeResDTO.Land land = item.land();
        if (land == null || land.name() == null || land.name().isBlank()) {
            return null;
        }
        String regionPrefix = buildRegionPrefix(item.region(), true);
        String number1 = land.number1() == null ? "" : land.number1();
        String number2 = (land.number2() == null || land.number2().isBlank()) ? "" : "-" + land.number2();
        String road = (land.name() + " " + number1 + number2).trim();
        if (road.isBlank()) {
            return null;
        }
        return (regionPrefix + " " + road).trim().replaceAll("\\s+", " ");
    }

    private Optional<String> resolveJibunAddress(NaverReverseGeocodeResDTO result) {
        return result.results().stream()
                .filter(item -> "addr".equalsIgnoreCase(item.name()))
                .map(item -> {
                    NaverReverseGeocodeResDTO.Land land = item.land();
                    if (land == null) {
                        return null;
                    }
                    String regionPrefix = buildRegionPrefix(item.region(), true);
                    String number1 = land.number1() == null ? "" : land.number1();
                    String number2 = (land.number2() == null || land.number2().isBlank()) ? "" : "-" + land.number2();
                    String jibun = (number1 + number2).trim();
                    if (jibun.isBlank()) {
                        return null;
                    }
                    return (regionPrefix + " " + jibun).trim().replaceAll("\\s+", " ");
                })
                .filter(jibun -> jibun != null && !jibun.isBlank())
                .findFirst();
    }

    private Optional<String> resolveRegionAddress(NaverReverseGeocodeResDTO result) {
        return result.results().stream()
                .sorted(Comparator.comparingInt(item -> {
                    if ("admcode".equalsIgnoreCase(item.name())) return 0;
                    if ("legalcode".equalsIgnoreCase(item.name())) return 1;
                    return 2;
                }))
                .map(NaverReverseGeocodeResDTO.ResultItem::region)
                .filter(region -> region != null)
                .map(region -> {
                    String area1 = region.area1() == null || region.area1().name() == null ? "" : region.area1().name();
                    String area2 = region.area2() == null || region.area2().name() == null ? "" : region.area2().name();
                    String area3 = region.area3() == null || region.area3().name() == null ? "" : region.area3().name();
                    String area4 = region.area4() == null || region.area4().name() == null ? "" : region.area4().name();
                    String full = (area1 + " " + area2 + " " + area3 + " " + area4).trim().replaceAll("\\s+", " ");
                    return full.isBlank() ? null : full;
                })
                .filter(regionAddress -> regionAddress != null && !regionAddress.isBlank())
                .findFirst();
    }

    private String buildRegionPrefix(NaverReverseGeocodeResDTO.Region region, boolean includeArea4) {
        if (region == null) {
            return "";
        }
        String area1 = region.area1() == null || region.area1().name() == null ? "" : region.area1().name();
        String area2 = region.area2() == null || region.area2().name() == null ? "" : region.area2().name();
        String area3 = region.area3() == null || region.area3().name() == null ? "" : region.area3().name();
        String area4 = region.area4() == null || region.area4().name() == null ? "" : region.area4().name();
        if (includeArea4) {
            return (area1 + " " + area2 + " " + area3 + " " + area4).trim().replaceAll("\\s+", " ");
        }
        return (area1 + " " + area2 + " " + area3).trim().replaceAll("\\s+", " ");
    }


    private String resolveSigungu(PGpoint point) {
        NaverReverseGeocodeResDTO response = naverMapReverseGeocodeService.reverseGeocode(point);

        return response.results().stream()
                // 행정동(admcode) 결과를 우선 사용하고, 없으면 다른 타입 결과 사용
                .sorted(Comparator.comparingInt(item -> "admcode".equalsIgnoreCase(item.name()) ? 0 : 1))
                .map(this::resolveAdministrativeComparisonKey)
                .filter(name -> name != null && !name.isBlank())
                .findFirst()
                .orElseThrow(() -> LocationException.of(LocationErrorCode.LOCATION_SIGUNGU_NOT_FOUND));
    }

    private String resolveAdministrativeComparisonKey(NaverReverseGeocodeResDTO.ResultItem item) {
        NaverReverseGeocodeResDTO.Region region = item.region();
        if (region == null) {
            return null;
        }

        String area1 = region.area1() == null ? null : region.area1().name();
        String area2 = region.area2() == null ? null : region.area2().name();
        String area3 = region.area3() == null ? null : region.area3().name();

        // 기본 비교 단위는 area2(시/군/구)
        if (area2 != null && !area2.isBlank()) {
            // 구가 별도로 area3에 내려오는 경우(예: 수원시 + 영통구)만 구까지 포함
            if (area3 != null && !area3.isBlank() && area3.endsWith("구")) {
                return (area2 + " " + area3).trim();
            }
            return area2.trim();
        }

        // 군/구가 없을 때는 "시" 단위로만 비교 (세종특별자치시 등)
        if (area1 != null && !area1.isBlank()
                && (area1.endsWith("시")
                || area1.contains("특별시")
                || area1.contains("광역시")
                || area1.contains("특별자치시"))) {
            return area1.trim();
        }

        return null;
    }
}
