package app.tinnie.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FirebaseAuthFilter extends OncePerRequestFilter {
  public static final String ATTRIBUTE_UID = "firebaseUid";
  public static final String ATTRIBUTE_EMAIL = "firebaseEmail";

  private final FirebaseAuth firebaseAuth;
  private final List<String> excludePaths;

  public FirebaseAuthFilter(FirebaseAuth firebaseAuth, List<String> excludePaths) {
    this.firebaseAuth = firebaseAuth;
    this.excludePaths = excludePaths;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    for (String exclude : excludePaths) {
      if (!exclude.isBlank() && path.startsWith(exclude)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      writeUnauthorized(response, "missing bearer token");
      return;
    }

    String token = authorization.substring("Bearer ".length()).trim();
    if (token.isEmpty()) {
      writeUnauthorized(response, "missing bearer token");
      return;
    }

    try {
      FirebaseToken decoded = firebaseAuth.verifyIdToken(token);
      request.setAttribute(ATTRIBUTE_UID, decoded.getUid());
      request.setAttribute(ATTRIBUTE_EMAIL, decoded.getEmail());
      filterChain.doFilter(request, response);
    } catch (FirebaseAuthException ex) {
      writeUnauthorized(response, "invalid bearer token");
    }
  }

  private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write("{\"error\":\"" + message + "\"}");
  }
}
