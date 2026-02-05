package app.tinnie.domain.routine.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoutineAiResponse {
  private List<RoutineAiRoutine> routines;
}
