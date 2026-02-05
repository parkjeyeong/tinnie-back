package app.tinnie.domain.lottery.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LotteryDto {
  private Integer series;
  private List<Integer> numbers;
}
