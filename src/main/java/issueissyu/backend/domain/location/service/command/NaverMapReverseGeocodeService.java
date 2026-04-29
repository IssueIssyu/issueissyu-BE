package issueissyu.backend.domain.location.service.command;

import issueissyu.backend.domain.location.dto.req.NaverReverseGeocodeReqDTO;
import issueissyu.backend.domain.location.dto.res.NaverReverseGeocodeResDTO;
import issueissyu.backend.domain.location.exception.LocationException;
import issueissyu.backend.domain.location.exception.code.LocationErrorCode;
import issueissyu.backend.global.config.properties.NaverMapProperties;
import org.postgresql.geometric.PGpoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Service
public class NaverMapReverseGeocodeService {

    private static final String REVERSE_GEOCODE_PATH = "/map-reversegeocode/v2/gc";
    private static final String REQUEST_TYPE = "coordsToaddr";
    private static final String OUTPUT_JSON = "json";
    private static final String DEFAULT_ORDERS = "legalcode,admcode,addr,roadaddr";

    private final RestClient naverMapRestClient;
    private final NaverMapProperties naverMapProperties;

    public NaverMapReverseGeocodeService(
            @Qualifier("naverMapRestClient") RestClient naverMapRestClient,
            NaverMapProperties naverMapProperties
    ) {
        this.naverMapRestClient = naverMapRestClient;
        this.naverMapProperties = naverMapProperties;
    }

    public NaverReverseGeocodeResDTO reverseGeocode(PGpoint point) {
        return reverseGeocode(NaverReverseGeocodeReqDTO.of(point));
    }

    public NaverReverseGeocodeResDTO reverseGeocode(NaverReverseGeocodeReqDTO request) {
        validateRequest(request);
        validateApiKeys();

        try {
            NaverReverseGeocodeResDTO response = naverMapRestClient.get()
                    .uri(uriBuilder -> buildReverseGeocodeUri(uriBuilder, request))
                    .header("x-ncp-apigw-api-key-id", naverMapProperties.getClientId())
                    .header("x-ncp-apigw-api-key", naverMapProperties.getClientSecret())
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(NaverReverseGeocodeResDTO.class);

            if (response == null || response.status() == null) {
                throw LocationException.of(LocationErrorCode.LOCATION_RESPONSE_EMPTY);
            }

            int statusCode = response.status().code();
            if (statusCode != 0 && statusCode != 3) {
                String message = response.status().message() == null ? "알 수 없는 오류" : response.status().message();
                throw new LocationException(LocationErrorCode.LOCATION_REVERSE_GEOCODE_API_FAILED,
                        "네이버 지도 리버스 지오코딩 요청 실패(" + statusCode + "): " + message);
            }

            List<NaverReverseGeocodeResDTO.ResultItem> results = Objects.requireNonNullElse(response.results(), List.of());
            return new NaverReverseGeocodeResDTO(response.status(), results);
        } catch (RestClientException e) {
            throw new LocationException(LocationErrorCode.LOCATION_REVERSE_GEOCODE_API_FAILED,
                    "네이버 지도 리버스 지오코딩 API 호출에 실패했습니다.", e);
        }
    }

    private URI buildReverseGeocodeUri(UriBuilder uriBuilder, NaverReverseGeocodeReqDTO request) {
        UriBuilder builder = uriBuilder
                .path(REVERSE_GEOCODE_PATH)
                .queryParam("request", REQUEST_TYPE)
                .queryParam("coords", request.toCoords())
                .queryParam("output", request.getOutput() == null || request.getOutput().isBlank() ? OUTPUT_JSON : request.getOutput())
                .queryParam("orders", request.getOrders() == null || request.getOrders().isBlank() ? DEFAULT_ORDERS : request.getOrders());

        if (request.getSourcecrs() != null && !request.getSourcecrs().isBlank()) {
            builder.queryParam("sourcecrs", request.getSourcecrs());
        }
        if (request.getTargetcrs() != null && !request.getTargetcrs().isBlank()) {
            builder.queryParam("targetcrs", request.getTargetcrs());
        }
        if (request.getCallback() != null && !request.getCallback().isBlank()) {
            builder.queryParam("callback", request.getCallback());
        }
        return builder.build();
    }

    private void validateRequest(NaverReverseGeocodeReqDTO request) {
        if (request == null || request.getPoint() == null || request.toCoords() == null || request.toCoords().isBlank()) {
            throw LocationException.of(LocationErrorCode.LOCATION_INVALID_REQUEST);
        }
    }

    private void validateApiKeys() {
        if (naverMapProperties.getClientId() == null || naverMapProperties.getClientId().isBlank()
                || naverMapProperties.getClientSecret() == null || naverMapProperties.getClientSecret().isBlank()) {
            throw LocationException.of(LocationErrorCode.LOCATION_API_KEY_MISSING);
        }
    }
}
