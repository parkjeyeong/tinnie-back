package app.tinnie.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfiguration {
  @Value("${firebase.credentials.path:}")
  private String credentialsPath;

  @Bean
  public FirebaseApp firebaseApp() throws IOException {
    if (!FirebaseApp.getApps().isEmpty()) {
      return FirebaseApp.getInstance();
    }

    GoogleCredentials credentials = loadCredentials();
    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(credentials)
        .build();

    return FirebaseApp.initializeApp(options);
  }

  private GoogleCredentials loadCredentials() throws IOException {
    if (credentialsPath != null && !credentialsPath.isBlank()) {
      InputStream is = new ClassPathResource(credentialsPath).getInputStream();
      return GoogleCredentials.fromStream(is);
    }
    return GoogleCredentials.getApplicationDefault();
  }
}
