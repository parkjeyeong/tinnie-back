package app.tinnie.mapper;

import app.tinnie.domain.routine.dto.RoutineLogDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface RoutineLogMapper {
  RoutineLogDto selectRoutineLog(@Param("routineId") long routineId, @Param("logDate") LocalDate logDate);

  List<RoutineLogDto> selectRoutineLogsByUserIdAndRange(@Param("userId") String userId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

  int upsertRoutineLog(@Param("routineId") long routineId,
                       @Param("logDate") LocalDate logDate,
                       @Param("status") String status);
}
