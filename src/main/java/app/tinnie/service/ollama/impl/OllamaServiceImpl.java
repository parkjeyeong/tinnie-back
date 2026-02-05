package app.tinnie.service.ollama.impl;

import app.tinnie.service.ollama.OllamaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OllamaServiceImpl implements OllamaService {
  private final ObjectMapper objectMapper;

  @Value("${ollama.cloud.base-url}")
  private String baseUrl;

  @Value("${ollama.cloud.api-key:}")
  private String apiKey;

  @Value("${ollama.cloud.timeout-seconds:60}")
  private long timeoutSeconds;

  @Value("${ollama.cloud.model}")
  private String defaultModel;

  private HttpClient httpClient;

  @PostConstruct
  void init() {
    httpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(timeoutSeconds))
      .build();
  }

  @Override
  public SseEmitter streamChat(String question, String model, Object options) {
    SseEmitter emitter = new SseEmitter(0L);

    if (baseUrl == null || baseUrl.isBlank()) {
      emitter.completeWithError(new IllegalStateException("ollama.cloud.base-url is not configured"));
      return emitter;
    }

    if (question == null || question.isBlank()) {
      emitter.completeWithError(new IllegalArgumentException("question is required"));
      return emitter;
    }

    String resolvedModel = resolveModel(model);
    if (resolvedModel == null) {
      emitter.completeWithError(new IllegalArgumentException("model is required"));
      return emitter;
    }

    Map<String, Object> requestPayload = new LinkedHashMap<>();
    requestPayload.put("model", resolvedModel);
    requestPayload.put("stream", true);
    requestPayload.put("messages", new Object[] {
      Map.of("role", "user", "content", question)
    });
    if (options != null) {
      requestPayload.put("options", options);
    }

    String requestBody;
    try {
      requestBody = objectMapper.writeValueAsString(requestPayload);
    } catch (JsonProcessingException ex) {
      emitter.completeWithError(ex);
      return emitter;
    }

    HttpRequest request = buildRequest(requestBody);

    CompletableFuture.runAsync(() -> streamResponse(request, emitter));

    return emitter;
  }

  @Override
  public String chat(String question, String model, Object options) {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new IllegalStateException("ollama.cloud.base-url is not configured");
    }

    if (question == null || question.isBlank()) {
      throw new IllegalArgumentException("question is required");
    }

    String resolvedModel = resolveModel(model);
    if (resolvedModel == null) {
      throw new IllegalArgumentException("model is required");
    }

    Map<String, Object> requestPayload = new LinkedHashMap<>();
    requestPayload.put("model", resolvedModel);
    requestPayload.put("stream", false);
    requestPayload.put("messages", new Object[] {
      Map.of("role", "user", "content", question)
    });
    if (options != null) {
      requestPayload.put("options", options);
    }

    String requestBody;
    try {
      requestBody = objectMapper.writeValueAsString(requestPayload);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to serialize request body", ex);
    }

    HttpRequest request = buildRequest(requestBody);

    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() / 100 != 2) {
        throw new IllegalStateException(
          "Ollama cloud request failed with status " + response.statusCode() + ": " + response.body()
        );
      }
      return extractContent(response.body());
    } catch (Exception ex) {
      throw new IllegalStateException("Ollama cloud request failed", ex);
    }
  }

  private String resolveModel(String model) {
    if (model != null && !model.isBlank()) {
      return model;
    }
    if (defaultModel != null && !defaultModel.isBlank()) {
      return defaultModel;
    }
    return null;
  }

  private HttpRequest buildRequest(String body) {
    HttpRequest.Builder builder = HttpRequest.newBuilder()
      .uri(URI.create(baseUrl))
      .timeout(Duration.ofSeconds(timeoutSeconds))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(body));

    if (apiKey != null && !apiKey.isBlank()) {
      builder.header("Authorization", "Bearer " + apiKey);
    }

    return builder.build();
  }

  private void streamResponse(HttpRequest request, SseEmitter emitter) {
    try {
      HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

      if (response.statusCode() / 100 != 2) {
        String errorBody = readError(response.body());
        emitter.completeWithError(new IllegalStateException(
          "Ollama cloud request failed with status " + response.statusCode() + ": " + errorBody
        ));
        return;
      }

      try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(response.body(), StandardCharsets.UTF_8)
      )) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.isBlank()) {
            continue;
          }
          emitter.send(SseEmitter.event().data(line));
          if (line.contains("\"done\":true")) {
            break;
          }
        }
      }

      emitter.complete();
    } catch (Exception ex) {
      emitter.completeWithError(ex);
    }
  }

  private String extractContent(String responseBody) throws JsonProcessingException {
    if (responseBody == null || responseBody.isBlank()) {
      return "";
    }

    Map<?, ?> payload = objectMapper.readValue(responseBody, Map.class);
    Object message = payload.get("message");
    if (message instanceof Map<?, ?> messageMap) {
      Object content = messageMap.get("content");
      if (content != null) {
        return content.toString();
      }
    }

    Object choices = payload.get("choices");
    if (choices instanceof List<?> choiceList && !choiceList.isEmpty()) {
      Object first = choiceList.get(0);
      if (first instanceof Map<?, ?> firstMap) {
        Object choiceMessage = firstMap.get("message");
        if (choiceMessage instanceof Map<?, ?> choiceMessageMap) {
          Object content = choiceMessageMap.get("content");
          if (content != null) {
            return content.toString();
          }
        }
        Object text = firstMap.get("text");
        if (text != null) {
          return text.toString();
        }
      }
    }

    Object response = payload.get("response");
    if (response != null) {
      return response.toString();
    }

    return responseBody;
  }

  private String readError(java.io.InputStream inputStream) throws IOException {
    if (inputStream == null) {
      return "";
    }

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      StringBuilder builder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        if (builder.length() > 0) {
          builder.append('\n');
        }
        builder.append(line);
      }
      return builder.toString();
    }
  }
}
