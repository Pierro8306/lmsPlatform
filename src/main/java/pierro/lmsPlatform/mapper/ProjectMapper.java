package pierro.lmsPlatform.mapper;

import org.mapstruct.Mapper;
import pierro.lmsPlatform.dto.response.Project.ProjectResponse;
import pierro.lmsPlatform.entity.project.Project;

import java.util.List;

@Mapper(componentModel = "spring", uses = IdMapper.class)
public interface ProjectMapper {
    ProjectResponse toResponse(Project project);

    List<ProjectResponse> toResponses(List<Project> projects);
}
