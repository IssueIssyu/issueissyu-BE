package issueissyu.backend.global.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Configuration
@ConditionalOnProperty(name = "billing.google-verification-enabled", havingValue = "true")
public class GooglePlayConfig {

    private static final String ANDROID_PUBLISHER_SCOPE = "https://www.googleapis.com/auth/androidpublisher";

    @Value("${google.play.credentials-base64:}")
    private String credentialsBase64;

    @Bean
    public AndroidPublisher androidPublisher() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = loadCredentials()
                .createScoped(ANDROID_PUBLISHER_SCOPE);

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        return new AndroidPublisher.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer
        ).setApplicationName("issueissyu").build();
    }

    private GoogleCredentials loadCredentials() throws IOException {
        if (!StringUtils.hasText(credentialsBase64)) {
            throw new IllegalStateException(
                    "billing.google-verification-enabled=true 이지만 GOOGLE_PLAY_CREDENTIALS_BASE64 가 비어 있습니다."
            );
        }

        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(credentialsBase64.replaceAll("\\s", ""));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("GOOGLE_PLAY_CREDENTIALS_BASE64 디코딩에 실패했습니다.", e);
        }

        try (InputStream inputStream = new ByteArrayInputStream(decoded)) {
            return GoogleCredentials.fromStream(inputStream);
        }
    }
}
