package issueissyu.backend.domain.location.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.location.dto.res.CoordinateLocationResolveResDTO;
import issueissyu.backend.domain.location.dto.res.LocationRegionListResDTO;
import issueissyu.backend.domain.location.dto.res.UserLocationCertResDto;
import issueissyu.backend.domain.location.dto.res.UserLocationResDTO;
import issueissyu.backend.domain.location.exception.code.LocationSuccessCode;
import issueissyu.backend.domain.location.service.LocationService;
import issueissyu.backend.domain.location.service.query.LocationRegionListQueryService;
import issueissyu.backend.global.api.ApiResponse;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.postgresql.geometric.PGpoint;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Location", description = "사용자 위치 인증/조회")
@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
    private final LocationRegionListQueryService locationRegionListQueryService;

    @Operation(
            summary = "지역구 목록 조회",
            description = "상위(비시군구) location 행을 제외한 지역구 목록과, 동네 인증 시 사용자 시군구 표시명을 반환합니다.")
    @GetMapping("/regions")
    public ApiResponse<LocationRegionListResDTO> getRegionList(@AuthenticationPrincipal String uid) {
        LocationRegionListResDTO body = locationRegionListQueryService.getRegionList(uid);
        LocationSuccessCode code =
                body.user() == null
                        ? LocationSuccessCode.LOCATION_LIST_204
                        : LocationSuccessCode.LOCATION_LIST_200;
        return ApiResponse.onSuccess(code, body);
    }

    @Operation(summary = "내 위치 조회")
    @GetMapping("/user")
    public ApiResponse<UserLocationCertResDto> getUserLocation(@AuthenticationPrincipal String uid) {
        UserLocationCertResDto result = locationService.getUserLocation(uid);
        return ApiResponse.onSuccess(LocationSuccessCode.LOCATION_USER_GET_SUCCESS, result);
    }

    @Operation(summary = "내 위치 인증")
    @PostMapping("/user/cert")
    public ApiResponse<UserLocationCertResDto> userLocationCert(
            @AuthenticationPrincipal String uid,
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng
    ) {
        UserLocationCertResDto result = locationService.userLocationCert(uid, toPoint(lat, lng));
        return ApiResponse.onSuccess(LocationSuccessCode.LOCATION_USER_CERT_SUCCESS, result);
    }

    @Operation(summary = "좌표 → 도로명 주소 조회")
    @GetMapping("/address")
    public ApiResponse<UserLocationResDTO> getRoadAddress(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng
    ) {
        UserLocationResDTO result = locationService.getRoadAddress(toPoint(lat, lng));
        return ApiResponse.onSuccess(LocationSuccessCode.LOCATION_ROAD_ADDRESS_SUCCESS, result);
    }

    @Operation(
            summary = "EPSG:4326 좌표 → 도로명 주소 및 location_id",
            description = "WGS84 위도(lat)·경도(lng). 네이버 역지오코딩의 roadaddr만 사용하며, 도로명이 없으면 오류를 반환합니다."
    )
    @GetMapping("/resolve")
    public ApiResponse<CoordinateLocationResolveResDTO> resolveAddressAndLocationId(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng
    ) {
        CoordinateLocationResolveResDTO result = locationService.resolveAddressAndLocationId(toPoint(lat, lng));
        return ApiResponse.onSuccess(LocationSuccessCode.LOCATION_COORDINATE_RESOLVE_SUCCESS, result);
    }

    @Operation(summary = "핀 생성 가능 여부 확인")
    @GetMapping("/pin/available")
    public ApiResponse<UserLocationResDTO> isUserCanPostPin(
            @AuthenticationPrincipal String uid,
            @RequestParam("userLat") double userLat,
            @RequestParam("userLng") double userLng,
            @RequestParam("pinLat") double pinLat,
            @RequestParam("pinLng") double pinLng
    ) {
        UserLocationResDTO result = locationService.isUserCanPostPin(
                uid,
                toPoint(userLat, userLng),
                toPoint(pinLat, pinLng)
        );
        return ApiResponse.onSuccess(LocationSuccessCode.LOCATION_PIN_CREATION_CHECK_SUCCESS, result);
    }

    private PGpoint toPoint(double lat, double lng) {
        validateLatLngCoordinate(lat, lng);
        // 내부 좌표계: x=경도(lng), y=위도(lat)
        return new PGpoint(lng, lat);
    }

    private void validateLatLngCoordinate(double lat, double lng) {
        if (Double.isNaN(lat) || Double.isNaN(lng) || Double.isInfinite(lat) || Double.isInfinite(lng)) {
            throw GeneralException.of(GeneralErrorCode.BAD_REQUEST);
        }
        // lat=위도[-90,90], lng=경도[-180,180]
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw GeneralException.of(GeneralErrorCode.BAD_REQUEST);
        }
    }
}
