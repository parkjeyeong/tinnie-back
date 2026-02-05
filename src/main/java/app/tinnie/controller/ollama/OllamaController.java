package app.tinnie.controller.ollama;

import app.tinnie.service.ollama.OllamaService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/ollama")
public class OllamaController {
  private final OllamaService ollamaService;

  @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter stream(@RequestBody OllamaRequest body) {
    return ollamaService.streamChat(body.question(), body.model(), body.options());
  }

  public record OllamaRequest(String question, String model, Object options) {
  }
}
