package issueissyu.backend.domain.location.service;

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
public class NaverMapService {

    private static final String GEOCODE_PATH = "/map-geocode/v2/geocode";

    @Qualifier("naverMapRestClient")
    private final RestClient naverMapRestClient;
    private final NaverMapProperties naverMapProperties;

    public GeocodeResult geocode(String query) {
        return geocode(new GeocodeRequest(query, null, null, null, null, null));
    }

    public GeocodeResult geocode(GeocodeRequest request) {
        validateRequest(request);
        validateApiKeys();

        try {
            GeocodeApiResponse response = naverMapRestClient.get()
                    .uri(uriBuilder -> buildGeocodeUri(uriBuilder, request))
                    .header("x-ncp-apigw-api-key-id", naverMapProperties.getClientId())
                    .header("x-ncp-apigw-api-key", naverMapProperties.getClientSecret())
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(GeocodeApiResponse.class);

            if (response == null) {
                throw new IllegalStateException("네이버 지도 지오코딩 응답이 비어 있습니다.");
            }
            if (response.status() == null || !"OK".equalsIgnoreCase(response.status())) {
                throw new IllegalStateException("네이버 지도 지오코딩 요청 실패: " + response.errorMessage());
            }

            List<GeocodeAddress> addresses = Objects.requireNonNullElse(response.addresses(), List.of());
            GeocodeMeta meta = response.meta() == null ? new GeocodeMeta(0, 1, addresses.size()) : response.meta();
            return new GeocodeResult(request.query(), meta, addresses);
        } catch (RestClientException e) {
            throw new IllegalStateException("네이버 지도 지오코딩 API 호출에 실패했습니다.", e);
        }
    }

    private URI buildGeocodeUri(UriBuilder uriBuilder, GeocodeRequest request) {
        UriBuilder builder = uriBuilder
                .path(GEOCODE_PATH)
                .queryParam("query", request.query());

        if (request.coordinate() != null && !request.coordinate().isBlank()) {
            builder.queryParam("coordinate", request.coordinate());
        }
        if (request.filter() != null && !request.filter().isBlank()) {
            builder.queryParam("filter", request.filter());
        }
        if (request.language() != null && !request.language().isBlank()) {
            builder.queryParam("language", request.language());
        }
        if (request.page() != null) {
            builder.queryParam("page", request.page());
        }
        if (request.count() != null) {
            builder.queryParam("count", request.count());
        }
        return builder.build();
    }

    private void validateRequest(GeocodeRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new IllegalArgumentException("query는 비어 있을 수 없습니다.");
        }
    }

    private void validateApiKeys() {
        if (naverMapProperties.getClientId() == null || naverMapProperties.getClientId().isBlank()
                || naverMapProperties.getClientSecret() == null || naverMapProperties.getClientSecret().isBlank()) {
            throw new IllegalStateException("Naver Map API 키가 설정되지 않았습니다.");
        }
    }

    public record GeocodeRequest(
            String query,
            String coordinate,
            String filter,
            String language,
            Integer page,
            Integer count
    ) {
    }

    public record GeocodeResult(
            String query,
            GeocodeMeta meta,
            List<GeocodeAddress> addresses
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeocodeApiResponse(
            String status,
            GeocodeMeta meta,
            List<GeocodeAddress> addresses,
            String errorMessage
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeocodeMeta(
            int totalCount,
            int page,
            int count
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeocodeAddress(
            String roadAddress,
            String jibunAddress,
            String englishAddress,
            List<AddressElement> addressElements,
            String x,
            String y,
            Double distance
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressElement(
            List<String> types,
            String longName,
            String shortName,
            String code
    ) {
    }
}
