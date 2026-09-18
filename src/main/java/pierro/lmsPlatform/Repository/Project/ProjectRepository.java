package pierro.lmsPlatform.Repository.Project;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Project.Project;

public interface ProjectRepository extends JpaRepository<Project,Long> {
}
