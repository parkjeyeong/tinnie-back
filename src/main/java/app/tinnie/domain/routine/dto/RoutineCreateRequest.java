package app.tinnie.domain.routine.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class RoutineCreateRequest {
  private String userId;
  private String title;
  private String goal;
  private String color;
  private LocalTime time;
  private LocalDate startDate;
  private LocalDate endDate;
  private Boolean isActive;
  private Boolean isNotify;
  private List<Integer> daysOfWeek;
}
