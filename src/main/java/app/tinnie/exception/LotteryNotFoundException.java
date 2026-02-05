package app.tinnie.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LotteryNotFoundException extends RuntimeException {
  // RuntimeException을 사용하는 이유
  // 1. 사용하는 곳에서 예외 처리를 강제하지 않음 ->try-catch가 필요 없음
  // 2. Spring의 예외 처리 방식과 자연스럽게 연결됨 -> @ExceptionHandler, @RestControllerAdvice 활용 가능
  // 3. 컨트롤러 코드가 더 깔끔해짐 -> 서비스 로직에 집중할 수 있음

  public LotteryNotFoundException(String type) {
    super("해당 타입의 복권이 존재하지 않습니다. : " + type);
  }
}
