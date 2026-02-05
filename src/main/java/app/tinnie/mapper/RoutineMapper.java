package app.tinnie.mapper;

import app.tinnie.domain.routine.dto.RoutineDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoutineMapper {
  RoutineDto selectRoutineById(@Param("id") long id);

  List<RoutineDto> selectRoutinesByUserId(@Param("userId") String userId);

  List<Integer> selectRoutineDays(@Param("routineId") long routineId);

  int insertRoutine(RoutineDto routine);

  int updateRoutine(RoutineDto routine);

  int deleteRoutine(@Param("id") long id);

  int deleteRoutineDays(@Param("routineId") long routineId);

  int insertRoutineDays(@Param("routineId") long routineId,
                        @Param("daysOfWeek") List<Integer> daysOfWeek);
}
