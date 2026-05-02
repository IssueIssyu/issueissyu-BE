package issueissyu.backend.domain.map.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.map.dto.res.MapPinResDTO;
import issueissyu.backend.domain.map.exception.code.MapSuccessCode;
import issueissyu.backend.domain.map.service.query.MapPinQueryService;
import issueissyu.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Map", description = "지도 API")
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapPinQueryService mapPinQueryService;

    @Operation(summary = "현재 화면 전체 핀 조회",
                description = "BBox(Bounding Box)를 이용해 현재 화면 내의 핀을 조회합니다. " +
                             "이슈는 등록 1년 이내, 소통 핀은 등록 1년 이내·최근 1개월 이내 반응, " +
                             "이벤트 핀(스토어·축제)은 설정된 게시 기간 내인 핀만 반환합니다.")
    @GetMapping("/pins")
    public ApiResponse<MapPinResDTO> getPinsInScreen(
            @RequestParam double swLat,
            @RequestParam double swLng,
            @RequestParam double neLat,
            @RequestParam double neLng
    ) {
        // PostGIS는 (경도, 위도) 순서 → swLng, swLat, neLng, neLat 로 전달
        return ApiResponse.onSuccess(
                MapSuccessCode.MAP_200,
                mapPinQueryService.getPinsInBoundingBox(swLng, swLat, neLng, neLat)
        );
    }
}
