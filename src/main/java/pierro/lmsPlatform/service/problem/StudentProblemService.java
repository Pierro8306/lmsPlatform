package pierro.lmsPlatform.service.problem;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.dto.request.Judge.JudgeRequest;
import pierro.lmsPlatform.dto.request.Judge.SubmitDto;
import pierro.lmsPlatform.dto.response.Judge.JudgeReport;
import pierro.lmsPlatform.dto.response.problem.ProblemSubmissionDetailResponse;
import pierro.lmsPlatform.dto.response.problem.ProblemSubmissionResponse;
import pierro.lmsPlatform.dto.response.problem.StudentProblemDetail;
import pierro.lmsPlatform.dto.response.problem.SubmissionTestResultDto;
import pierro.lmsPlatform.entity.auth.User;
import pierro.lmsPlatform.entity.problem.Problem;
import pierro.lmsPlatform.entity.problem.Submission;
import pierro.lmsPlatform.entity.problem.SubmissionTestResult;
import pierro.lmsPlatform.entity.problem.TestCase;
import pierro.lmsPlatform.entity.problem.Verdict;
import pierro.lmsPlatform.repository.auth.UserRepository;
import pierro.lmsPlatform.repository.problem.ProblemRepository;
import pierro.lmsPlatform.repository.problem.SubmissionRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.BadRequestException;
import pierro.lmsPlatform.security.exception.ConflictException;
import pierro.lmsPlatform.security.exception.ForbiddenException;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentProblemService {
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final IdObfuscator ids;

    // 1. Lấy danh sách bài tập được giao cho học viên
    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(Long userId) {

        Long groupId = groupIdOf(userId);

        return problemRepository.findByPublishedTrueAndGroups_Id(groupId)
                .stream()
                .map(p -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", ids.encode(p.getId()));
                    map.put("title", p.getTitle());
                    map.put("difficulty", p.getDifficulty());
                    map.put("deadline", p.getDeadline());
                    map.put("open", isOpen(p));
                    map.put("maxAttempts", p.getMaxAttempts());
                    map.put("attemptsUsed", submissionRepository.countByProblemIdAndUserId(p.getId(), userId));
                    return map;
                })
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

        long usedAttempts = submissionRepository.countByProblemIdAndUserId(p.getId(), userId);

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
                (int) usedAttempts,
                samples
        );
    }

    /**
     * Kiểm tra bài tập có được giao cho học viên không.
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
     * Kiểm tra thời gian và số lần nộp bài cho phép.
     */
    @Transactional(readOnly = true)
    public Problem validateCanSubmit(Long userId, String problemIdH) {
        Long problemId = ids.decode(problemIdH);
        Problem p = requireAccessible(userId, problemId);
        checkWindow(p);

        if (p.getMaxAttempts() != null) {
            long used = submissionRepository.countByProblemIdAndUserId(p.getId(), userId);
            if (used >= p.getMaxAttempts()) {
                throw new ConflictException("Bạn đã dùng hết số lần nộp bài (" + p.getMaxAttempts() + ")");
            }
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

    @Transactional(readOnly = true)
    public JudgeRequest createJudgeRequest(SubmitDto submitDto) {
        Problem problem = problemRepository.findById(ids.decode(submitDto.idProblem()))
                .filter(Problem::isPublished)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Problem not found: " + submitDto.idProblem()
                        )
                );
        return createJudgeRequest(problem, submitDto);
    }

    public JudgeRequest createJudgeRequest(Problem problem, SubmitDto submitDto) {
        List<JudgeRequest.TestCaseData> testCases = problem.getTestCases().stream()
                .sorted(Comparator.comparingLong(TestCase::getId))
                .map(tc -> new JudgeRequest.TestCaseData(
                        tc.getId(),
                        tc.getInputData(),
                        tc.getExpectedOutput(),
                        tc.getScoreWeight(),
                        tc.isHidden()
                ))
                .toList();

        return new JudgeRequest(
                submitDto.sourceCode(),
                submitDto.language(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitKb(),
                testCases,
                false,
                0
        );
    }

    @Transactional
    public Submission saveSubmission(JudgeReport judgeReport, SubmitDto submitDto, Long userId) {
        Long problemId = ids.decode(submitDto.idProblem());
        Problem problem = problemRepository.findById(problemId)
                .filter(Problem::isPublished)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Problem not found: " + submitDto.idProblem()
                        )
                );
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        Submission submission = new Submission();
        submission.setProblem(problem);
        submission.setLanguage(submitDto.language());
        submission.setCreatedAt(LocalDateTime.now());
        submission.setSourceCode(submitDto.sourceCode());
        submission.setStatus(judgeReport.verdict());
        submission.setMemoryUsedKb(judgeReport.maxMemoryKb());
        submission.setExecTimeMs(judgeReport.maxTimeMs());
        submission.setUser(user);

        Map<Long, TestCase> testCaseMap = problem.getTestCases().stream()
                .collect(Collectors.toMap(TestCase::getId, tc -> tc));

        if (judgeReport.results() != null) {
            for (JudgeReport.TestResult tr : judgeReport.results()) {
                TestCase tc = testCaseMap.get(tr.id());
                if (tc != null) {
                    SubmissionTestResult str = new SubmissionTestResult();
                    str.setSubmission(submission);
                    str.setTestCase(tc);
                    str.setStatus(tr.verdict());
                    str.setExecTimeMs(tr.timeMs());
                    str.setMemoryUsedKb(tr.memoryKb());
                    str.setErrorMessage(tr.message());
                    submission.getTestResults().add(str);
                }
            }
        }

        return submissionRepository.save(submission);
    }

    // Lấy danh sách lịch sử nộp bài của học viên cho 1 bài tập
    @Transactional(readOnly = true)
    public List<ProblemSubmissionResponse> listSubmissions(Long userId, String problemIdH) {
        Long problemId = ids.decode(problemIdH);
        requireAccessible(userId, problemId);

        return submissionRepository.findByProblemIdAndUserIdOrderByCreatedAtDesc(problemId, userId)
                .stream()
                .map(this::toSubmissionResponse)
                .toList();
    }

    // Lấy chi tiết một lần nộp bài của học viên
    @Transactional(readOnly = true)
    public ProblemSubmissionDetailResponse getSubmissionDetail(Long userId, String problemIdH, String submissionIdH) {
        Long problemId = ids.decode(problemIdH);
        requireAccessible(userId, problemId);

        Long submissionId = ids.decode(submissionIdH);
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .filter(s -> s.getProblem().getId().equals(problemId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài nộp"));

        return toSubmissionDetailResponse(submission, false);
    }

    private ProblemSubmissionResponse toSubmissionResponse(Submission s) {
        int total = s.getTestResults() != null ? s.getTestResults().size() : 0;
        int passed = s.getTestResults() != null ? (int) s.getTestResults().stream().filter(tr -> tr.getStatus() == Verdict.AC).count() : 0;

        return ProblemSubmissionResponse.builder()
                .id(ids.encode(s.getId()))
                .problemId(ids.encode(s.getProblem().getId()))
                .problemTitle(s.getProblem().getTitle())
                .userId(ids.encode(s.getUser().getId()))
                .username(s.getUser().getUsername())
                .language(s.getLanguage())
                .status(s.getStatus())
                .execTimeMs(s.getExecTimeMs())
                .memoryUsedKb(s.getMemoryUsedKb())
                .createdAt(s.getCreatedAt())
                .totalTests(total)
                .passedTests(passed)
                .build();
    }

    public ProblemSubmissionDetailResponse toSubmissionDetailResponse(Submission s, boolean isAdmin) {
        int total = s.getTestResults() != null ? s.getTestResults().size() : 0;
        int passed = s.getTestResults() != null ? (int) s.getTestResults().stream().filter(tr -> tr.getStatus() == Verdict.AC).count() : 0;

        List<SubmissionTestResultDto> testResultDtos = s.getTestResults() == null ? List.of() : s.getTestResults().stream()
                .sorted(Comparator.comparing(tr -> tr.getTestCase().getId()))
                .map(tr -> SubmissionTestResultDto.builder()
                        .id(ids.encode(tr.getId()))
                        .testCaseId(ids.encode(tr.getTestCase().getId()))
                        .status(tr.getStatus())
                        .execTimeMs(tr.getExecTimeMs())
                        .memoryUsedKb(tr.getMemoryUsedKb())
                        .errorMessage((!tr.getTestCase().isHidden() || isAdmin) ? tr.getErrorMessage() : "Hidden test")
                        .hidden(tr.getTestCase().isHidden())
                        .build())
                .toList();

        return ProblemSubmissionDetailResponse.builder()
                .id(ids.encode(s.getId()))
                .problemId(ids.encode(s.getProblem().getId()))
                .problemTitle(s.getProblem().getTitle())
                .userId(ids.encode(s.getUser().getId()))
                .username(s.getUser().getUsername())
                .language(s.getLanguage())
                .sourceCode(s.getSourceCode())
                .status(s.getStatus())
                .execTimeMs(s.getExecTimeMs())
                .memoryUsedKb(s.getMemoryUsedKb())
                .createdAt(s.getCreatedAt())
                .totalTests(total)
                .passedTests(passed)
                .testResults(testResultDtos)
                .build();
    }
}
