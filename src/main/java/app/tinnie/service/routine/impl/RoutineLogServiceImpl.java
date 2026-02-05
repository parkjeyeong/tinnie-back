package app.tinnie.service.routine.impl;

import app.tinnie.domain.routine.dto.RoutineDto;
import app.tinnie.domain.routine.dto.RoutineLogDto;
import app.tinnie.exception.RoutineNotFoundException;
import app.tinnie.mapper.RoutineLogMapper;
import app.tinnie.mapper.RoutineMapper;
import app.tinnie.service.routine.RoutineLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RoutineLogServiceImpl implements RoutineLogService {
  private static final String STATUS_DONE = "DONE";
  private static final String STATUS_MISS = "MISS";

  private final RoutineLogMapper routineLogMapper;
  private final RoutineMapper routineMapper;

  @Override
  public RoutineLogDto toggleRoutine(long routineId, LocalDate logDate) {
    if (logDate == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "logDate is required");
    }

    RoutineDto routine = routineMapper.selectRoutineById(routineId);
    if (routine == null) {
      throw new RoutineNotFoundException(routineId);
    }

    RoutineLogDto existing = routineLogMapper.selectRoutineLog(routineId, logDate);
    String nextStatus = resolveNextStatus(existing);

    routineLogMapper.upsertRoutineLog(routineId, logDate, nextStatus);
    return routineLogMapper.selectRoutineLog(routineId, logDate);
  }

  private String resolveNextStatus(RoutineLogDto existing) {
    if (existing == null) {
      return STATUS_DONE;
    }
    if (STATUS_DONE.equals(existing.getStatus())) {
      return STATUS_MISS;
    }
    return STATUS_DONE;
  }

}
