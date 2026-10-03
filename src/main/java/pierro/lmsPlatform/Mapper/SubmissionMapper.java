package pierro.lmsPlatform.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pierro.lmsPlatform.DTO.Response.Project.SubmissionDto;
import pierro.lmsPlatform.Entity.Project.ProjectSubmission;

import java.util.List;

@Mapper(componentModel = "spring", uses = IdMapper.class)
public interface SubmissionMapper {
    List<SubmissionDto> listEntityToListDTO(List<ProjectSubmission> submissions);

    @Mapping(source = "user.username", target = "userName")
    SubmissionDto entityToDTO(ProjectSubmission submission);
}
