package issueissyu.backend.domain.location.service.command;

import issueissyu.backend.domain.location.dto.req.NaverGeocodeReqDTO;
import issueissyu.backend.domain.location.dto.res.NaverGeocodeResDTO;
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

@Service
public class NaverMapGeocodeService {

    private static final String GEOCODE_PATH = "/map-geocode/v2/geocode";

    private final RestClient naverMapRestClient;
    private final NaverMapProperties naverMapProperties;

    public NaverMapGeocodeService(
            @Qualifier("naverMapRestClient") RestClient naverMapRestClient,
            NaverMapProperties naverMapProperties
    ) {
        this.naverMapRestClient = naverMapRestClient;
        this.naverMapProperties = naverMapProperties;
    }

    public NaverGeocodeResDTO geocode(String query) {
        return geocode(NaverGeocodeReqDTO.of(query));
    }

    public PGpoint geocodeToPoint(String query) {
        NaverGeocodeReqDTO req = NaverGeocodeReqDTO.builder()
                .query(query)
                .language("kor")
                .page(1)
                .count(1)
                .build();
        NaverGeocodeResDTO result = geocode(req);
        return result.firstPoint()
                .orElseThrow(() -> LocationException.of(LocationErrorCode.LOCATION_ADDRESS_NOT_FOUND));
    }

    public NaverGeocodeResDTO geocode(NaverGeocodeReqDTO request) {
        validateRequest(request);
        validateApiKeys();

        try {
            NaverGeocodeResDTO response = naverMapRestClient.get()
                    .uri(uriBuilder -> buildGeocodeUri(uriBuilder, request))
                    .header("x-ncp-apigw-api-key-id", naverMapProperties.getClientId())
                    .header("x-ncp-apigw-api-key", naverMapProperties.getClientSecret())
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(NaverGeocodeResDTO.class);

            if (response == null) {
                throw LocationException.of(LocationErrorCode.LOCATION_RESPONSE_EMPTY);
            }
            if (response.status() == null || !"OK".equalsIgnoreCase(response.status())) {
                throw LocationException.of(LocationErrorCode.LOCATION_GEOCODE_API_FAILED);
            }
            return response;
        } catch (RestClientException e) {
            throw LocationException.of(LocationErrorCode.LOCATION_GEOCODE_API_FAILED);
        }
    }

    private URI buildGeocodeUri(UriBuilder uriBuilder, NaverGeocodeReqDTO request) {
        UriBuilder builder = uriBuilder
                .path(GEOCODE_PATH)
                .queryParam("query", request.getQuery());

        if (request.getCoordinate() != null && !request.getCoordinate().isBlank()) {
            builder.queryParam("coordinate", request.getCoordinate());
        }
        if (request.getFilter() != null && !request.getFilter().isBlank()) {
            builder.queryParam("filter", request.getFilter());
        }
        if (request.getLanguage() != null && !request.getLanguage().isBlank()) {
            builder.queryParam("language", request.getLanguage());
        }
        if (request.getPage() != null) {
            builder.queryParam("page", request.getPage());
        }
        if (request.getCount() != null) {
            builder.queryParam("count", request.getCount());
        }
        return builder.build();
    }

    private void validateRequest(NaverGeocodeReqDTO request) {
        if (request == null || request.getQuery() == null || request.getQuery().isBlank()) {
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
