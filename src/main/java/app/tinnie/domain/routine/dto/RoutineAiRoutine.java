package app.tinnie.domain.routine.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoutineAiRoutine {
  private String title;
  private String goal;
  private String color;
  private String time;
  private String startDate;
  private String endDate;
  private Boolean isActive;
  private Boolean isNotify;
  private List<Integer> daysOfWeek;
}
