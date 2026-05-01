package issueissyu.backend.domain.location.controller;

import issueissyu.backend.domain.location.service.NaverMapService;
import issueissyu.backend.domain.location.exception.code.LocationSuccessCode;
import issueissyu.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.postgresql.geometric.PGpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


//Todo-서비스 로직 테스트이후 삭제 필요
@RestController
@RequestMapping("/dev/location/naver")
@RequiredArgsConstructor
public class NaverMapTestController {

    private final NaverMapService naverMapService;

    @GetMapping("/sigungu-match")
    public ApiResponse<SigunguMatchResDTO> isSameSigungu(
            @RequestParam double x1,
            @RequestParam double y1,
            @RequestParam double x2,
            @RequestParam double y2
    ) {
        PGpoint first = new PGpoint(x1, y1);
        PGpoint second = new PGpoint(x2, y2);
        boolean same = naverMapService.isSameSigungu(first, second);
        String firstAddress = naverMapService.resolveRoadAddressOf(first);
        String secondAddress = naverMapService.resolveRoadAddressOf(second);

        return ApiResponse.onSuccess(
                LocationSuccessCode.LOCATION_SIGUNGU_MATCH_SUCCESS,
                new SigunguMatchResDTO(first, second, firstAddress, secondAddress, same)
        );
    }

    @GetMapping("/geocode")
    public ApiResponse<GeocodeResDTO> geocode(@RequestParam String address) {
        PGpoint point = naverMapService.geocodeToPoint(address);
        return ApiResponse.onSuccess(
                LocationSuccessCode.LOCATION_GEOCODE_SUCCESS,
                new GeocodeResDTO(address, point.x, point.y)
        );
    }

    public record SigunguMatchResDTO(
            PGpoint firstPoint,
            PGpoint secondPoint,
            String firstAddress,
            String secondAddress,
            boolean sameSigungu
    ) {
    }

    public record GeocodeResDTO(
            String address,
            double x,
            double y
    ) {
    }
}
