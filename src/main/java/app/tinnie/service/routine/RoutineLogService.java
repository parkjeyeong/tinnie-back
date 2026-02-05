package app.tinnie.service.routine;

import app.tinnie.domain.routine.dto.RoutineLogDto;

import java.time.LocalDate;

public interface RoutineLogService {
  RoutineLogDto toggleRoutine(long routineId, LocalDate logDate);
}
