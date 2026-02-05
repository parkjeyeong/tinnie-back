package app.tinnie.domain.routine.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoutineAiCreateRequest {
  private String userId;
  private String prompt;
  private String intensity;
}
