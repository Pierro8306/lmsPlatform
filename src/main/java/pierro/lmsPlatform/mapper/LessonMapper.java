package pierro.lmsPlatform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pierro.lmsPlatform.dto.request.Learning.CreateLessonDto;
import pierro.lmsPlatform.dto.response.Learning.LessonDetailDto;
import pierro.lmsPlatform.entity.learning.Lesson;

import java.util.List;

@Mapper(componentModel = "spring", uses = IdMapper.class)
public interface LessonMapper {
    LessonDetailDto entityToDTO(Lesson lesson);

    List<LessonDetailDto> entityToDTO(List<Lesson> lessons);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    Lesson toEntity(CreateLessonDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    void updateEntity(CreateLessonDto dto, @MappingTarget Lesson lesson);
}
