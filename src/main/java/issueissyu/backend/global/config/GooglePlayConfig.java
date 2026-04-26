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
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

@Configuration
@ConditionalOnProperty(name = "billing.google-verification-enabled", havingValue = "true")
public class GooglePlayConfig {

    @Value("${google.play.credentials-path}")
    private Resource credentialsResource;

    @Bean
    public AndroidPublisher androidPublisher() throws IOException, GeneralSecurityException {
        try (InputStream inputStream = credentialsResource.getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped("https://www.googleapis.com/auth/androidpublisher");

            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
            return new AndroidPublisher.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    requestInitializer
            ).setApplicationName("issueissyu").build();
        }
    }
}
