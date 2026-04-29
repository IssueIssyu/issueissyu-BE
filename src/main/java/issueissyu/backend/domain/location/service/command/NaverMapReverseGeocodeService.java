package issueissyu.backend.domain.location.service.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import issueissyu.backend.global.config.properties.NaverMapProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NaverMapReverseGeocodeService {

    private static final String REVERSE_GEOCODE_PATH = "/map-reversegeocode/v2/gc";
    private static final String OUTPUT_JSON = "json";
    private static final String DEFAULT_ORDERS = "legalcode,admcode,addr,roadaddr";

    @Qualifier("naverMapRestClient")
    private final RestClient naverMapRestClient;
    private final NaverMapProperties naverMapProperties;

    public ReverseGeocodeResult reverseGeocode(String coords) {
        return reverseGeocode(new ReverseGeocodeRequest(coords, null, null, null, OUTPUT_JSON, null));
    }

    public ReverseGeocodeResult reverseGeocode(ReverseGeocodeRequest request) {
        validateRequest(request);
        validateApiKeys();

        try {
            ReverseGeocodeApiResponse response = naverMapRestClient.get()
                    .uri(uriBuilder -> buildReverseGeocodeUri(uriBuilder, request))
                    .header("x-ncp-apigw-api-key-id", naverMapProperties.getClientId())
                    .header("x-ncp-apigw-api-key", naverMapProperties.getClientSecret())
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(ReverseGeocodeApiResponse.class);

            if (response == null || response.status() == null) {
                throw new IllegalStateException("네이버 지도 리버스 지오코딩 응답이 비어 있습니다.");
            }

            int statusCode = response.status().code();
            if (statusCode != 0 && statusCode != 3) {
                String message = response.status().message() == null ? "알 수 없는 오류" : response.status().message();
                throw new IllegalStateException("네이버 지도 리버스 지오코딩 요청 실패(" + statusCode + "): " + message);
            }

            List<ReverseGeocodeItem> results = Objects.requireNonNullElse(response.results(), List.of());
            return new ReverseGeocodeResult(response.status(), results);
        } catch (RestClientException e) {
            throw new IllegalStateException("네이버 지도 리버스 지오코딩 API 호출에 실패했습니다.", e);
        }
    }

    private URI buildReverseGeocodeUri(UriBuilder uriBuilder, ReverseGeocodeRequest request) {
        UriBuilder builder = uriBuilder
                .path(REVERSE_GEOCODE_PATH)
                .queryParam("coords", request.coords())
                .queryParam("output", request.output() == null || request.output().isBlank() ? OUTPUT_JSON : request.output())
                .queryParam("orders", request.orders() == null || request.orders().isBlank() ? DEFAULT_ORDERS : request.orders());

        if (request.sourcecrs() != null && !request.sourcecrs().isBlank()) {
            builder.queryParam("sourcecrs", request.sourcecrs());
        }
        if (request.targetcrs() != null && !request.targetcrs().isBlank()) {
            builder.queryParam("targetcrs", request.targetcrs());
        }
        if (request.callback() != null && !request.callback().isBlank()) {
            builder.queryParam("callback", request.callback());
        }
        return builder.build();
    }

    private void validateRequest(ReverseGeocodeRequest request) {
        if (request == null || request.coords() == null || request.coords().isBlank()) {
            throw new IllegalArgumentException("coords는 비어 있을 수 없습니다. 예: 127.1054328,37.3595963");
        }
    }

    private void validateApiKeys() {
        if (naverMapProperties.getClientId() == null || naverMapProperties.getClientId().isBlank()
                || naverMapProperties.getClientSecret() == null || naverMapProperties.getClientSecret().isBlank()) {
            throw new IllegalStateException("Naver Map API 키가 설정되지 않았습니다.");
        }
    }

    public record ReverseGeocodeRequest(
            String coords,
            String sourcecrs,
            String targetcrs,
            String orders,
            String output,
            String callback
    ) {
    }

    public record ReverseGeocodeResult(
            ReverseGeocodeStatus status,
            List<ReverseGeocodeItem> results
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ReverseGeocodeApiResponse(
            ReverseGeocodeStatus status,
            List<ReverseGeocodeItem> results
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReverseGeocodeStatus(
            int code,
            String name,
            String message
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReverseGeocodeItem(
            String name,
            ReverseGeocodeCode code,
            ReverseGeocodeRegion region,
            ReverseGeocodeLand land
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReverseGeocodeCode(
            String id,
            String type,
            String mappingId
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReverseGeocodeRegion(
            ReverseGeocodeArea area0,
            ReverseGeocodeArea area1,
            ReverseGeocodeArea area2,
            ReverseGeocodeArea area3,
            ReverseGeocodeArea area4
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReverseGeocodeArea(
            String name,
            String alias,
            ReverseGeocodeCoords coords
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReverseGeocodeLand(
            String type,
            String name,
            String number1,
            String number2,
            ReverseGeocodeCoords coords,
            ReverseGeocodeAddition addition0,
            ReverseGeocodeAddition addition1,
            ReverseGeocodeAddition addition2,
            ReverseGeocodeAddition addition3,
            ReverseGeocodeAddition addition4
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReverseGeocodeAddition(
            String type,
            String value
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReverseGeocodeCoords(
            ReverseGeocodeCenter center
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReverseGeocodeCenter(
            String crs,
            Double x,
            Double y
    ) {
    }
}
