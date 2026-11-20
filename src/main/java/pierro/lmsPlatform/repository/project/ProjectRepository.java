package pierro.lmsPlatform.repository.project;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pierro.lmsPlatform.entity.project.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @EntityGraph(attributePaths = "userGroup")
    List<Project> findAllByOrderByDeadlineDesc();

    @EntityGraph(attributePaths = "userGroup")
    List<Project> findByUserGroupIsNullOrderByDeadlineDesc();

    @EntityGraph(attributePaths = "userGroup")
    @Query("select p from Project p where p.userGroup is null or p.userGroup.id = :groupId order by p.deadline desc")
    List<Project> findAvailableForGroup(@Param("groupId") Long groupId);

    @Override
    @EntityGraph(attributePaths = "userGroup")
    Optional<Project> findById(Long id);
}
