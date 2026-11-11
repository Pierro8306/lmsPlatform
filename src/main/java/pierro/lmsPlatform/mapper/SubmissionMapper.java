package pierro.lmsPlatform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pierro.lmsPlatform.dto.response.Project.SubmissionDto;
import pierro.lmsPlatform.entity.project.ProjectSubmission;

import java.util.List;

@Mapper(componentModel = "spring", uses = IdMapper.class)
public interface SubmissionMapper {
    List<SubmissionDto> listEntityToListDTO(List<ProjectSubmission> submissions);

    @Mapping(source = "user.username", target = "userName")
    SubmissionDto entityToDTO(ProjectSubmission submission);
}
