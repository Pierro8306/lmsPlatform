package pierro.lmsPlatform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pierro.lmsPlatform.dto.response.Project.ProjectResponse;
import pierro.lmsPlatform.entity.project.Project;

import java.util.List;

@Mapper(componentModel = "spring", uses = IdMapper.class)
public interface ProjectMapper {
    @Mapping(source = "userGroup.name", target = "userGroup")
    ProjectResponse toResponse(Project project);

    List<ProjectResponse> toResponses(List<Project> projects);
}
