package app.tinnie.config.firebase;

import app.tinnie.filter.FirebaseAuthFilter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class FirebaseAuthFilterConfiguration {
  @Value("${firebase.auth.exclude-paths:}")
  private String excludePaths;

  @Bean
  public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
    return FirebaseAuth.getInstance(firebaseApp);
  }

  @Bean
  public FilterRegistrationBean<FirebaseAuthFilter> firebaseAuthFilter(FirebaseAuth firebaseAuth) {
    List<String> exclusions = parseExclusions(excludePaths);
    FirebaseAuthFilter filter = new FirebaseAuthFilter(firebaseAuth, exclusions);

    FilterRegistrationBean<FirebaseAuthFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
    registration.addUrlPatterns("/api/*");
    return registration;
  }

  private List<String> parseExclusions(String raw) {
    if (raw == null || raw.isBlank()) {
      return List.of();
    }

    return Arrays.stream(raw.split(","))
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .collect(Collectors.toList());
  }
}
