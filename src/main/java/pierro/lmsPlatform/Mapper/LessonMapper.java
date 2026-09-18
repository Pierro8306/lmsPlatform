package pierro.lmsPlatform.Mapper;

import org.mapstruct.Mapper;
import pierro.lmsPlatform.DTO.Response.Learning.LessonDetailDto;
import pierro.lmsPlatform.Entity.Learning.Lesson;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    List<LessonDetailDto> entityToDTO(List<Lesson> lesson);
}
