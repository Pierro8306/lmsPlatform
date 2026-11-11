package pierro.lmsPlatform.repository.notification;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.communication.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEvent_IdAndUser_Id(Long eventId, Long userId);

    void deleteByEvent_IdAndUser_Id(Long eventId, Long userId);

    @EntityGraph(attributePaths = "user")
    List<Member> findByEvent_Id(Long eventId);
}
