package pierro.lmsPlatform.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pierro.lmsPlatform.DTO.Response.Project.SubmissionDto;
import pierro.lmsPlatform.Entity.Project.ProjectSubmission;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;
// Import IdObfuscator của bạn vào đây
// import pierro.lmsPlatform.Utils.IdObfuscator;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class SubmissionMapper {

    @Autowired
    protected IdObfuscator idObfuscator;

    public abstract List<SubmissionDto> listEntityToListDTO(List<ProjectSubmission> projectSubmissions);

    @Mapping(source = "user.username", target = "userName")
    @Mapping(target = "id", expression = "java(idObfuscator.encode(projectSubmissions.getId()))")
    public abstract SubmissionDto entityToDTO(ProjectSubmission projectSubmissions);
}
