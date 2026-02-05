package app.tinnie.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RoutineNotFoundException extends RuntimeException {
  public RoutineNotFoundException(Long id) {
    super("루틴을 찾을 수 없습니다. id=" + id);
  }
}
