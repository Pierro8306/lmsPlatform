package pierro.lmsPlatform.Mapper;

import org.mapstruct.Mapper;
import pierro.lmsPlatform.DTO.Response.Project.ProjectResponse;
import pierro.lmsPlatform.Entity.Project.Project;

import java.util.List;

@Mapper(componentModel = "spring", uses = IdMapper.class)
public interface ProjectMapper {
    ProjectResponse toResponse(Project project);

    List<ProjectResponse> toResponses(List<Project> projects);
}
