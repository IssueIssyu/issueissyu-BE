package issueissyu.backend.domain.pin.util;

import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.config.AmazonConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PinS3UrlSupport {

    // 버킷 공개 URL({@code https://{bucket}.s3.{region}.amazonaws.com/...})에서 S3 object key를 추출합니다.
    public static String extractKey(String url, AmazonConfig config) {
        return extractKey(url, config, PinErrorCode.PIN_IMPORT_COMMUNICATION_400_2);
    }

    public static String extractKey(String url, AmazonConfig config, BaseErrorCode violation) {
        if (url == null || url.isBlank()) {
            throw PinException.of(violation);
        }
        String cdnUrl = config.getCdnUrl();
        String cdnPrefix = (cdnUrl != null && !cdnUrl.isBlank())
                ? (cdnUrl.endsWith("/") ? cdnUrl : cdnUrl + "/")
                : null;
        String prefix =
                "https://" + config.getBucket() + ".s3." + config.getRegion() + ".amazonaws.com/";
        String encoded;
        if (cdnPrefix != null && url.startsWith(cdnPrefix)) {
            encoded = url.substring(cdnPrefix.length());
        } else if (url.startsWith(prefix)) {
            encoded = url.substring(prefix.length());
        } else {
            throw PinException.of(violation);
        }
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }
}
