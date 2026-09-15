package pierro.lmsPlatform.Mapper;

import org.mapstruct.Mapper;
import pierro.lmsPlatform.DTO.Response.Quiz.ChoiceResponse;
import pierro.lmsPlatform.Entity.Quiz.QuestionChoice;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChoiceMapper {
   List<ChoiceResponse> entityToDTO(List<QuestionChoice> questionChoice);
}
