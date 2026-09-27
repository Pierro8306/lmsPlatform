package pierro.lmsPlatform.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pierro.lmsPlatform.DTO.Response.Quiz.StudentChoiceResponse;
import pierro.lmsPlatform.Entity.Quiz.QuestionChoice;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ChoiceMapper {
   @Autowired
   protected IdObfuscator idObfuscator;
   @Mapping(target = "id", expression = "java(idObfuscator.encode(choice.getId()))")
   public abstract StudentChoiceResponse toStudentDto(QuestionChoice choice);

   public abstract List<StudentChoiceResponse> toStudentDtoList(List<QuestionChoice> choices);
}