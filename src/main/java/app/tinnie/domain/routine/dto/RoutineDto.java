package app.tinnie.domain.routine.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class RoutineDto {
  private Long id;
  private String userId;
  private String title;
  private String goal;
  private String time;
  private LocalDate startDate;
  private LocalDate endDate;
  private String color;
  private Boolean isActive;
  private Boolean isNotify;
  private LocalDateTime createdAt;
  private List<Integer> daysOfWeek;
  private List<RoutineLogDto> logStatuses;
}
