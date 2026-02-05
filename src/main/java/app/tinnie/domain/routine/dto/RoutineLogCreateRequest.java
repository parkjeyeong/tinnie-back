package app.tinnie.domain.routine.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RoutineLogCreateRequest {
  private LocalDate logDate;
}
