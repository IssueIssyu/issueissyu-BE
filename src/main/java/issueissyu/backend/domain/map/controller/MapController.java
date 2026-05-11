package issueissyu.backend.domain.map.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.map.dto.res.MapNoticeListResDTO;
import issueissyu.backend.domain.map.dto.res.MapPinCardResDTO;
import issueissyu.backend.domain.map.dto.res.MapPinResDTO;
import issueissyu.backend.domain.map.dto.res.PatchNoteResDTO;
import issueissyu.backend.domain.map.enums.MapPinCategory;
import issueissyu.backend.domain.map.exception.MapException;
import issueissyu.backend.domain.map.exception.code.MapErrorCode;
import issueissyu.backend.domain.map.exception.code.MapSuccessCode;
import issueissyu.backend.domain.map.service.query.MapNoticeQueryService;
import issueissyu.backend.domain.map.service.query.MapPinCardQueryService;
import issueissyu.backend.domain.map.service.query.MapPinQueryService;
import issueissyu.backend.domain.map.service.query.PatchNoteQueryService;
import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Tag(name = "Map", description = "지도 API")
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapPinQueryService mapPinQueryService;
    private final MapPinCardQueryService mapPinCardQueryService;
    private final MapNoticeQueryService mapNoticeQueryService;
    private final PatchNoteQueryService patchNoteQueryService;

    @Operation(summary = "현재 화면 핀 조회",
                description = "BBox(Bounding Box)를 이용해 현재 화면 내의 핀을 조회합니다. ")
    @GetMapping("/pins")
    public ApiResponse<MapPinResDTO> getPinsInScreen(
            @RequestParam double swLat,
            @RequestParam double swLng,
            @RequestParam double neLat,
            @RequestParam double neLng,
            @RequestParam(required = false) String category
    ) {
        if (swLat > neLat || swLng > neLng) {
            throw MapException.of(MapErrorCode.MAP_400_1);
        }

        Optional<MapPinCategory> categoryOpt = MapPinCategory.parse(category);
        MapSuccessCode successCode = categoryOpt
                .map(MapPinCategory::getSuccessCode)
                .orElse(MapSuccessCode.MAP_200_1);
        String pinTypeFilter = categoryOpt.map(MapPinCategory::getPinType).orElse(null);

        // PostGIS는 (경도, 위도) 순서 → swLng, swLat, neLng, neLat 로 전달
        return ApiResponse.onSuccess(
                successCode,
                mapPinQueryService.getPinsInBoundingBox(swLng, swLat, neLng, neLat, pinTypeFilter)
        );
    }

    @Operation(summary = "지도 공지사항 조회", description = "공지 시작 ~ 종료 시각 사이의 공지만 반환합니다.")
    @GetMapping("/notices")
    public ApiResponse<MapNoticeListResDTO> getMapNotices() {
        MapNoticeListResDTO body = mapNoticeQueryService.getActiveNotices();
        if (body.notices().isEmpty()) {
            return ApiResponse.onSuccess(MapSuccessCode.MAP_NOTICE_204, null);
        }
        return ApiResponse.onSuccess(MapSuccessCode.MAP_NOTICE_200, body);
    }

    @Operation(
            summary = "패치노트 조회",
            description = "이슈 핀만 반환. 정렬은 해결전, 해결중, 해결 완료 순 + 오래된 순. region 생략 시 /api/location/user 과 동일 기준(인증된 시군구 명)을 사용합니다.")
    @GetMapping("/patch-note")
    public ApiResponse<PatchNoteResDTO> getPatchNotes(
            @AuthenticationPrincipal String uid,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursor) {
        return ApiResponse.onSuccess(
                MapSuccessCode.PATCHNOTE_200,
                patchNoteQueryService.getPatchNotes(uid, region, size, cursor));
    }

    @Operation(
            summary = "단일 핀 카드 조회",
            description = "필수 쿼리 pinId. communityId는 연결된 커뮤니티가 있을 때만 반환.")
    @GetMapping("/card")
    public ApiResponse<MapPinCardResDTO> getPinCard(
            @AuthenticationPrincipal String uid, @RequestParam Long pinId) {
        MapPinCardResDTO dto = mapPinCardQueryService.findPinCard(pinId, uid);
        return ApiResponse.onSuccess(MapSuccessCode.forPinCard(PinType.valueOf(dto.getPinType())), dto);
    }
}
