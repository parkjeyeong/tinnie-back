package app.tinnie.controller.routine;

import app.tinnie.domain.routine.dto.*;
import app.tinnie.service.routine.RoutineLogService;
import app.tinnie.service.routine.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routines")
public class RoutineController {
  private final RoutineService routineService;
  private final RoutineLogService routineLogService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RoutineDto createRoutineManual(@RequestBody RoutineCreateRequest request) {
    return routineService.createRoutineManual(request);
  }

  @PostMapping("/ai")
  @ResponseStatus(HttpStatus.CREATED)
  public List<RoutineDto> createRoutineAi(@RequestBody RoutineAiCreateRequest request) {
    return routineService.createRoutineAi(request);
  }

  @GetMapping("/{id}")
  public RoutineDto getRoutine(@PathVariable long id) {
    return routineService.getRoutine(id);
  }

  @GetMapping
  public List<RoutineDto> listRoutines(@RequestParam String userId,
                                       @RequestParam(required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                       LocalDate startDate,
                                       @RequestParam(required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                       LocalDate endDate) {
    return routineService.listRoutines(userId, startDate, endDate);
  }

  @PutMapping("/{id}")
  public RoutineDto updateRoutine(@PathVariable long id, @RequestBody RoutineUpdateRequest request) {
    return routineService.updateRoutine(id, request);
  }

  @PostMapping("/{id}/logs/toggle")
  public RoutineLogDto toggleRoutine(@PathVariable long id, @RequestBody RoutineLogCreateRequest request) {
    if (request == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
    }
    return routineLogService.toggleRoutine(id, request.getLogDate());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteRoutine(@PathVariable long id) {
    routineService.deleteRoutine(id);
  }
}
