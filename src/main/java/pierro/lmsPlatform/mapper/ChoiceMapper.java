package pierro.lmsPlatform.mapper;

import org.mapstruct.Mapper;
import pierro.lmsPlatform.dto.response.Quiz.StudentChoiceResponse;
import pierro.lmsPlatform.entity.quiz.QuestionChoice;

import java.util.List;

@Mapper(componentModel = "spring", uses = IdMapper.class)
public interface ChoiceMapper {
    StudentChoiceResponse toStudentDto(QuestionChoice choice);

    List<StudentChoiceResponse> toStudentDtoList(List<QuestionChoice> choices);
}
