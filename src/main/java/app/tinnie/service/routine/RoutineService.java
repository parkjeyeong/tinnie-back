package app.tinnie.service.routine;

import app.tinnie.domain.routine.dto.RoutineCreateRequest;
import app.tinnie.domain.routine.dto.RoutineDto;
import app.tinnie.domain.routine.dto.RoutineUpdateRequest;
import app.tinnie.domain.routine.dto.RoutineAiCreateRequest;

import java.time.LocalDate;
import java.util.List;

public interface RoutineService {
  RoutineDto createRoutineManual(RoutineCreateRequest request);

  List<RoutineDto> createRoutineAi(RoutineAiCreateRequest request);

  RoutineDto getRoutine(long id);

  List<RoutineDto> listRoutines(String userId, LocalDate startDate, LocalDate endDate);

  RoutineDto updateRoutine(long id, RoutineUpdateRequest request);

  void deleteRoutine(long id);
}
