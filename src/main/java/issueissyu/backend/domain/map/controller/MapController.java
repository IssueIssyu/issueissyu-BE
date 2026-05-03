package issueissyu.backend.domain.map.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.map.dto.res.MapPinResDTO;
import issueissyu.backend.domain.map.enums.MapPinCategory;
import issueissyu.backend.domain.map.exception.MapException;
import issueissyu.backend.domain.map.exception.code.MapErrorCode;
import issueissyu.backend.domain.map.exception.code.MapSuccessCode;
import issueissyu.backend.domain.map.service.query.MapPinQueryService;
import issueissyu.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
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
}
