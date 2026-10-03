package pierro.lmsPlatform.Mapper;

import org.mapstruct.Mapper;
import pierro.lmsPlatform.DTO.Response.Quiz.StudentChoiceResponse;
import pierro.lmsPlatform.Entity.Quiz.QuestionChoice;

import java.util.List;

@Mapper(componentModel = "spring", uses = IdMapper.class)
public interface ChoiceMapper {
    StudentChoiceResponse toStudentDto(QuestionChoice choice);

    List<StudentChoiceResponse> toStudentDtoList(List<QuestionChoice> choices);
}
