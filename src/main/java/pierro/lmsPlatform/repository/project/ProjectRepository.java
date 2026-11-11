package pierro.lmsPlatform.repository.project;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.project.Project;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project,Long> {

    List<Project> findAllByOrderByDeadlineDesc();
}
