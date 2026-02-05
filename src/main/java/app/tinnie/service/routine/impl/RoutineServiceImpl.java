package app.tinnie.service.routine.impl;

import app.tinnie.domain.routine.dto.*;
import app.tinnie.exception.RoutineNotFoundException;
import app.tinnie.mapper.RoutineLogMapper;
import app.tinnie.mapper.RoutineMapper;
import app.tinnie.service.ollama.OllamaService;
import app.tinnie.service.routine.RoutineService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RoutineServiceImpl implements RoutineService {
  private final RoutineMapper routineMapper;
  private final RoutineLogMapper routineLogMapper;
  private final OllamaService ollamaService;
  private final ObjectMapper objectMapper;
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter FLEX_TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

  @Override
  public RoutineDto createRoutineManual(RoutineCreateRequest request) {
    validateCreateRequest(request);

    RoutineDto routine = new RoutineDto();
    routine.setUserId(request.getUserId().trim());
    routine.setTitle(request.getTitle().trim());
//    routine.setGoal(request.getGoal());
    routine.setColor(request.getColor());
    routine.setTime(formatTime(request.getTime()));
    routine.setStartDate(request.getStartDate());
    routine.setEndDate(request.getEndDate());
    routine.setIsActive(resolveIsActive(request.getIsActive()));
    routine.setIsNotify(request.getIsNotify());

    routineMapper.insertRoutine(routine);

    List<Integer> days = normalizeDays(request.getDaysOfWeek());
    if (days != null && !days.isEmpty()) {
      routineMapper.insertRoutineDays(routine.getId(), days);
    }

    return getRoutine(routine.getId());
  }

  @Override
  public List<RoutineDto> createRoutineAi(RoutineAiCreateRequest request) {
    validateAiCreateRequest(request);

    String userId = request.getUserId().trim();
    LocalDate startDate = LocalDate.now();
    String prompt = buildAiPrompt(request.getPrompt().trim(), request.getIntensity().trim(), startDate);
    String rawResponse = ollamaService.chat(prompt, null, null);
    RoutineAiResponse aiResponse = parseAiResponse(rawResponse);

    if (aiResponse == null || aiResponse.getRoutines() == null || aiResponse.getRoutines().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI response is empty");
    }

    List<RoutineDto> created = new ArrayList<>();
    for (RoutineAiRoutine aiRoutine : aiResponse.getRoutines()) {
      RoutineDto routine = buildRoutineFromAi(userId, aiRoutine);
      if (routine == null) {
        continue;
      }

      routineMapper.insertRoutine(routine);

      List<Integer> days = normalizeAiDays(aiRoutine.getDaysOfWeek());
      if (!days.isEmpty()) {
        routineMapper.insertRoutineDays(routine.getId(), days);
      }

      created.add(getRoutine(routine.getId()));
    }

    if (created.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI response did not contain valid routines");
    }

    return created;
  }

  @Override
  public RoutineDto getRoutine(long id) {
    RoutineDto routine = routineMapper.selectRoutineById(id);
    if (routine == null) {
      throw new RoutineNotFoundException(id);
    }

    routine.setDaysOfWeek(routineMapper.selectRoutineDays(id));
    return routine;
  }

  @Override
  public List<RoutineDto> listRoutines(String userId, LocalDate startDate, LocalDate endDate) {
    if (userId == null || userId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
    }
    LocalDate resolvedStartDate = startDate != null ? startDate : LocalDate.now();
    LocalDate resolvedEndDate = endDate != null ? endDate : resolvedStartDate;
    validateDateRange(resolvedStartDate, resolvedEndDate);

    List<RoutineDto> routines = routineMapper.selectRoutinesByUserId(userId);
    Map<Long, List<RoutineLogDto>> logsByRoutineId =
        groupLogsByRoutineId(routineLogMapper.selectRoutineLogsByUserIdAndRange(userId, resolvedStartDate, resolvedEndDate));

    for (RoutineDto routine : routines) {
      routine.setDaysOfWeek(routineMapper.selectRoutineDays(routine.getId()));
      routine.setLogStatuses(logsByRoutineId.getOrDefault(routine.getId(), new ArrayList<>()));
    }
    return routines;
  }

  @Override
  public RoutineDto updateRoutine(long id, RoutineUpdateRequest request) {
    if (request == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
    }

    RoutineDto existing = routineMapper.selectRoutineById(id);
    if (existing == null) {
      throw new RoutineNotFoundException(id);
    }

    RoutineDto updated = new RoutineDto();
    updated.setId(id);
    updated.setUserId(existing.getUserId());
    updated.setTitle(resolveTitle(request.getTitle(), existing.getTitle()));
//    updated.setGoal(resolveGoal(request.getGoal(), existing.getGoal()));
    updated.setColor(resolveColor(request.getColor(), existing.getColor()));
    updated.setTime(resolveTime(request.getTime(), existing.getTime()));
    updated.setStartDate(resolveStartDate(request.getStartDate(), existing.getStartDate()));
    updated.setEndDate(resolveEndDate(request.getEndDate(), existing.getEndDate()));
    updated.setIsActive(resolveIsActive(request.getIsActive() != null ? request.getIsActive() : existing.getIsActive()));
    updated.setIsNotify(resolveIsNotify(request.getIsNotify(), existing.getIsNotify()));

    validateDateRange(updated.getStartDate(), updated.getEndDate());

    routineMapper.updateRoutine(updated);

    if (request.getDaysOfWeek() != null) {
      List<Integer> days = normalizeDays(request.getDaysOfWeek());
      routineMapper.deleteRoutineDays(id);
      if (days != null && !days.isEmpty()) {
        routineMapper.insertRoutineDays(id, days);
      }
    }

    return getRoutine(id);
  }

  @Override
  public void deleteRoutine(long id) {
    int deleted = routineMapper.deleteRoutine(id);
    if (deleted == 0) {
      throw new RoutineNotFoundException(id);
    }
  }

  private void validateCreateRequest(RoutineCreateRequest request) {
    if (request == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
    }
    if (request.getUserId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
    }
    if (request.getUserId().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
    }
    if (request.getTitle() == null || request.getTitle().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
    }
    if (request.getTime() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "time is required");
    }
    if (request.getStartDate() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate is required");
    }
    validateDateRange(request.getStartDate(), request.getEndDate());
    normalizeDays(request.getDaysOfWeek());
  }

  private void validateAiCreateRequest(RoutineAiCreateRequest request) {
    if (request == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
    }
    if (request.getUserId() == null || request.getUserId().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
    }
    if (request.getPrompt() == null || request.getPrompt().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "prompt is required");
    }
    if (request.getIntensity() == null || request.getIntensity().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "intensity is required");
    }
    String normalized = request.getIntensity().trim().toLowerCase();
    if (!normalized.equals("high") && !normalized.equals("mid") && !normalized.equals("low")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "intensity must be high, mid, or low");
    }
  }

  private String buildAiPrompt(String prompt, String intensity, LocalDate startDate) {
    return """
      당신은 사용자의 목표를 해석해
      구체적이고 실행 가능한 루틴으로 분해하는 AI입니다.
      
      아래 JSON 형식만 반환하세요.
      다른 텍스트는 절대 포함하지 마세요.
      
      형식:
      {"routines":[
        {
          "title":"",
          "color":"#FFFFFF",
          "time":"HH:mm",
          "startDate":"yyyy-MM-dd",
          "endDate":null,
          "isActive":true,
          "isNotify":false,
          "daysOfWeek":[1,2,3]
        }
      ]}
      
      기본 규칙:
      - routines는 1개 이상 5개 이하
      - 날짜는 yyyy-MM-dd, 시간은 HH:mm (24시간)
      - daysOfWeek는 1(월)~7(일)
      - intensity는 수행 빈도를 의미함
        - high: 주 5~7일
        - mid: 주 3~5일
        - low: 주 1~3일
      - intensity에 맞게 daysOfWeek를 추천할 것
      - 사용자의 별도 요청이 없으면 최대한 주말을 제외한 주중에 균등한 간격으로 daysOfWeek를 설정할 것
      - 부족한 값은 null 또는 생략 가능
      - 루틴은 한글로 표현
      
      품질 규칙 (중요):
      - title은 반드시 "무엇을 어떻게 할지"가 포함된 실행 가능한 행동 단위여야 함
      - 아래와 같은 추상적인 표현만 있는 title은 금지
        (예: 열심히 하기, 꾸준히 하기, 운동하기, 공부하기)
      - 만든 루틴의 내용은 중복되지 않아야 함
      
      도메인별 규칙:
      - 목표가 운동인 경우:
        title에 운동 종류, 시간 또는 강도 중 1개 이상 포함
      - 목표가 공부/학습인 경우:
        title에 학습 대상(과목/주제)과 학습 방식 포함
        (예: 문제풀이, 강의 시청, 요약 정리)
      - 목표가 독서/자기계발인 경우:
        title에 분량 또는 시간 기준 포함
      
      예시 (운동 루틴):
      {"routines":[
        {
          "title":"스쿼트·푸시업·플랭크 각 3세트 (총 40분)",
          "color":"#FF6B6B",
          "time":"19:00",
          "startDate":"2026-02-05",
          "endDate":null,
          "isActive":true,
          "isNotify":false,
          "daysOfWeek":[1,3,5]
        }
      ]}
      
      예시 (공부 루틴):
      {"routines":[
        {
          "title":"백준 실버 난이도 문제 3개 풀이 및 오답 정리",
          "color":"#4D96FF",
          "time":"21:00",
          "startDate":"2026-02-05",
          "endDate":null,
          "isActive":true,
          "isNotify":false,
          "daysOfWeek":[2,4,6]
        }
      ]}

      사용자 프롬프트: %s
      intensity: %s
      루틴 시작일: %s
      """.formatted(prompt, intensity, startDate);
  }

  private RoutineAiResponse parseAiResponse(String rawResponse) {
    if (rawResponse == null || rawResponse.isBlank()) {
      return null;
    }

    try {
      return objectMapper.readValue(rawResponse, RoutineAiResponse.class);
    } catch (JsonProcessingException ex) {
      String extracted = extractJsonObject(rawResponse);
      if (extracted == null) {
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI response is not valid JSON");
      }
      try {
        return objectMapper.readValue(extracted, RoutineAiResponse.class);
      } catch (JsonProcessingException nested) {
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI response is not valid JSON");
      }
    }
  }

  private String extractJsonObject(String rawResponse) {
    int start = rawResponse.indexOf('{');
    int end = rawResponse.lastIndexOf('}');
    if (start < 0 || end <= start) {
      return null;
    }
    return rawResponse.substring(start, end + 1);
  }

  private RoutineDto buildRoutineFromAi(String userId, RoutineAiRoutine aiRoutine) {
    if (aiRoutine == null || aiRoutine.getTitle() == null || aiRoutine.getTitle().isBlank()) {
      return null;
    }

    String resolvedTitle = aiRoutine.getTitle().trim();
//    String resolvedGoal = aiRoutine.getGoal() != null && !aiRoutine.getGoal().isBlank()
//      ? aiRoutine.getGoal().trim()
//      : resolvedTitle;

    LocalDate startDate = parseDateOrDefault(aiRoutine.getStartDate(), LocalDate.now());
    LocalDate endDate = parseDateOrDefault(aiRoutine.getEndDate(), null);
    if (endDate != null && endDate.isBefore(startDate)) {
      endDate = null;
    }

    RoutineDto routine = new RoutineDto();
    routine.setUserId(userId);
    routine.setTitle(resolvedTitle);
//    routine.setGoal(resolvedGoal);
    routine.setColor(aiRoutine.getColor());
    routine.setTime(parseTimeOrDefault(aiRoutine.getTime(), "00:00"));
    routine.setStartDate(startDate);
    routine.setEndDate(endDate);
    routine.setIsActive(resolveIsActive(aiRoutine.getIsActive()));
    routine.setIsNotify(aiRoutine.getIsNotify());
    return routine;
  }

  private void validateDateRange(LocalDate startDate, LocalDate endDate) {
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must be on or after startDate");
    }
  }

  private List<Integer> normalizeDays(List<Integer> days) {
    if (days == null) {
      return null;
    }

    LinkedHashSet<Integer> normalized = new LinkedHashSet<>();
    for (Integer day : days) {
      if (day == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "daysOfWeek contains null");
      }
      if (day < 1 || day > 7) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dayOfWeek must be between 1 and 7");
      }
      normalized.add(day);
    }

    return new ArrayList<>(normalized);
  }

  private List<Integer> normalizeAiDays(List<Integer> days) {
    if (days == null) {
      return new ArrayList<>();
    }

    LinkedHashSet<Integer> normalized = new LinkedHashSet<>();
    for (Integer day : days) {
      if (day == null) {
        continue;
      }
      if (day < 1 || day > 7) {
        continue;
      }
      normalized.add(day);
    }

    return new ArrayList<>(normalized);
  }

  private LocalDate parseDateOrDefault(String value, LocalDate fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    try {
      return LocalDate.parse(value.trim());
    } catch (DateTimeParseException ex) {
      return fallback;
    }
  }

  private String parseTimeOrDefault(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    try {
      LocalTime time = LocalTime.parse(value.trim(), FLEX_TIME_FORMATTER);
      return time.format(TIME_FORMATTER);
    } catch (DateTimeParseException ex) {
      return fallback;
    }
  }

  private Boolean resolveIsActive(Boolean value) {
    return value != null ? value : Boolean.TRUE;
  }

  private Boolean resolveIsNotify(Boolean candidate, Boolean fallback) {
    return candidate != null ? candidate : fallback;
  }

  private String resolveTitle(String candidate, String fallback) {
    if (candidate == null) {
      return fallback;
    }
    if (candidate.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank");
    }
    return candidate.trim();
  }

  private String resolveGoal(String candidate, String fallback) {
    if (candidate == null) {
      return fallback;
    }
    return candidate;
  }

  private String resolveColor(String candidate, String fallback) {
    if (candidate == null) {
      return fallback;
    }
    return candidate;
  }

  private String resolveTime(LocalTime candidate, String fallback) {
    return candidate != null ? formatTime(candidate) : fallback;
  }

  private LocalDate resolveStartDate(LocalDate candidate, LocalDate fallback) {
    return candidate != null ? candidate : fallback;
  }

  private LocalDate resolveEndDate(LocalDate candidate, LocalDate fallback) {
    return candidate != null ? candidate : fallback;
  }

  private String formatTime(LocalTime time) {
    return time != null ? time.format(TIME_FORMATTER) : null;
  }

  private Map<Long, List<RoutineLogDto>> groupLogsByRoutineId(List<RoutineLogDto> logs) {
    Map<Long, List<RoutineLogDto>> grouped = new HashMap<>();
    for (RoutineLogDto log : logs) {
      grouped.computeIfAbsent(log.getRoutineId(), key -> new ArrayList<>()).add(log);
    }
    return grouped;
  }
}
