package pierro.lmsPlatform.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pierro.lmsPlatform.DTO.Response.Learning.LessonDetailDto;
import pierro.lmsPlatform.DTO.Response.Project.SubmissionDto;
import pierro.lmsPlatform.Entity.Learning.Lesson;
import pierro.lmsPlatform.Entity.Project.ProjectSubmission;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {
    @Mapping(source = "user.username", target = "userName")
    List<SubmissionDto> listEntityToListDTO(List<ProjectSubmission> projectSubmissions);
    SubmissionDto entityToDTO(ProjectSubmission projectSubmissions);
}
