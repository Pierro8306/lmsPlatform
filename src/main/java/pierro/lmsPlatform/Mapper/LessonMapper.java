package pierro.lmsPlatform.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pierro.lmsPlatform.DTO.Response.Learning.LessonDetailDto;
import pierro.lmsPlatform.Entity.Learning.Lesson;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class LessonMapper {

    @Autowired
    protected IdObfuscator idObfuscator;
    @Mapping(target = "id", expression = "java(idObfuscator.encode(Lesson.getId()))")
    public abstract List<LessonDetailDto> entityToDTO(List<Lesson> lesson);
}
