package issueissyu.backend.domain.pin.util;

import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.global.config.AmazonConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PinS3UrlSupport {

    // 버킷 공개 URL({@code https://{bucket}.s3.{region}.amazonaws.com/...})에서 S3 object key를 추출합니다.
    public static String extractKey(String url, AmazonConfig config) {
        if (url == null || url.isBlank()) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_COMMUNICATION_400_2);
        }
        String prefix =
                "https://" + config.getBucket() + ".s3." + config.getRegion() + ".amazonaws.com/";
        if (!url.startsWith(prefix)) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_COMMUNICATION_400_2);
        }
        String encoded = url.substring(prefix.length());
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }
}
