package app.tinnie.domain.routine.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class RoutineLogDto {
  private Long id;
  private Long routineId;
  private LocalDate logDate;
  private String status;
  private LocalDateTime checkedAt;
}
