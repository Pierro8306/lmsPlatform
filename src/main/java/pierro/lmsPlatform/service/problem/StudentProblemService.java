package pierro.lmsPlatform.service.problem;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pierro.lmsPlatform.dto.response.problem.StudentProblemDetail;
import pierro.lmsPlatform.entity.auth.User;
import pierro.lmsPlatform.entity.problem.Problem;
import pierro.lmsPlatform.repository.auth.UserRepository;
import pierro.lmsPlatform.repository.problem.ProblemRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.BadRequestException;
import pierro.lmsPlatform.security.exception.ForbiddenException;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentProblemService {
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final IdObfuscator ids;

    // 1. Lấy danh sách bài tập được giao cho học viên
    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(Long userId) {

        Long groupId = groupIdOf(userId);

        return problemRepository.findByPublishedTrueAndGroups_Id(groupId)
                .stream()
                .map(p -> Map.<String, Object>of(
                        "id", ids.encode(p.getId()),
                        "title", p.getTitle(),
                        "difficulty", String.valueOf(p.getDifficulty()),
                        "deadline", String.valueOf(p.getDeadline()),
                        "open", isOpen(p)
                ))
                .toList();
    }

    // 2. Xem chi tiết bài tập
    @Transactional(readOnly = true)
    public StudentProblemDetail detail(Long userId, String problemIdH) {

        Problem p = requireAccessible(
                userId,
                ids.decode(problemIdH)
        );

        var samples = p.getTestCases()
                .stream()
                .filter(t -> !t.isHidden())
                .map(t -> new StudentProblemDetail.SampleTest(
                        t.getInputData(),
                        t.getExpectedOutput()
                ))
                .toList();

        return new StudentProblemDetail(
                ids.encode(p.getId()),
                p.getTitle(),
                p.getContent(),
                p.getDifficulty(),
                p.getTimeLimitMs(),
                p.getMemoryLimitKb(),
                p.getStartAt(),
                p.getDeadline(),
                isOpen(p),
                p.getMaxAttempts(),
                samples
        );
    }

    /**
     * Kiểm tra bài tập có được giao cho học viên không.
     * Dùng lại ở Bước 3 khi nộp bài.
     */
    @Transactional(readOnly = true)
    public Problem requireAccessible(Long userId, Long problemId) {

        Long groupId = groupIdOf(userId);

        Problem p = problemRepository.findById(problemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy bài tập"
                        )
                );

        boolean assigned = p.getGroups()
                .stream()
                .anyMatch(g -> g.getId().equals(groupId));

        if (!p.isPublished() || !assigned) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy bài tập"
            );
        }

        return p;
    }

    /**
     * Kiểm tra thời gian được phép nộp bài.
     *
     * @return true nếu được nộp muộn
     */
    public boolean checkWindow(Problem p) {

        LocalDateTime now = LocalDateTime.now();

        // Chưa đến thời gian mở bài
        if (p.getStartAt() != null
                && now.isBefore(p.getStartAt())) {

            throw new BadRequestException(
                    "Bài tập chưa mở"
            );
        }

        // Đã quá deadline
        if (p.getDeadline() != null
                && now.isAfter(p.getDeadline())) {

            // Không cho phép nộp muộn
            if (!Boolean.TRUE.equals(p.getAllowLate())) {
                throw new BadRequestException(
                        "Đã quá hạn nộp bài"
                );
            }

            // Cho phép nộp muộn
            return true;
        }

        return false;
    }

    // Kiểm tra bài tập có đang trong thời gian cho phép truy cập/nộp không
    private boolean isOpen(Problem p) {

        try {
            checkWindow(p);
            return true;
        } catch (BadRequestException e) {
            return false;
        }
    }

    // Lấy nhóm của học viên và kiểm tra quyền truy cập
    private Long groupIdOf(Long userId) {

        User u = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy người dùng"
                        )
                );

        // Kiểm tra đúng vai trò học viên
        if (u.getRole() == null
                || !"STUDENT".equalsIgnoreCase(u.getRole().getName())) {
            throw new ForbiddenException(
                    "Bạn không có quyền truy cập chức năng này"
            );
        }

        // Kiểm tra học viên đã được xếp nhóm chưa
        if (u.getUserGroup() == null) {
            throw new ForbiddenException(
                    "Bạn chưa thuộc nhóm nào"
            );
        }
        return u.getUserGroup().getId();
    }

}
