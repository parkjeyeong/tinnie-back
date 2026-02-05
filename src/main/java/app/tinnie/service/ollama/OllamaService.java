package app.tinnie.service.ollama;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface OllamaService {
  SseEmitter streamChat(String question, String model, Object options);

  String chat(String question, String model, Object options);
}
