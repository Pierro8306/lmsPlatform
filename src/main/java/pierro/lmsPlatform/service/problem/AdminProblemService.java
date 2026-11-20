package pierro.lmsPlatform.service.problem;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.dto.request.problem.ProblemRequest;
import pierro.lmsPlatform.dto.request.problem.TestCaseItemRequest;
import pierro.lmsPlatform.dto.request.problem.TestCaseRequest;
import pierro.lmsPlatform.dto.response.problem.ProblemResponse;
import pierro.lmsPlatform.dto.response.problem.ProblemSubmissionDetailResponse;
import pierro.lmsPlatform.dto.response.problem.ProblemSubmissionResponse;
import pierro.lmsPlatform.dto.response.problem.SubmissionTestResultDto;
import pierro.lmsPlatform.dto.response.problem.TestCaseResponse;
import pierro.lmsPlatform.entity.problem.Problem;
import pierro.lmsPlatform.entity.problem.Submission;
import pierro.lmsPlatform.entity.problem.TestCase;
import pierro.lmsPlatform.entity.problem.Verdict;
import pierro.lmsPlatform.repository.auth.UserGroupRepository;
import pierro.lmsPlatform.repository.problem.ProblemRepository;
import pierro.lmsPlatform.repository.problem.SubmissionRepository;
import pierro.lmsPlatform.repository.problem.TestCaseRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.BadRequestException;
import pierro.lmsPlatform.security.exception.ConflictException;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminProblemService {
    private final ProblemRepository problemrepository;
    private final TestCaseRepository testCaseRepository;
    private final UserGroupRepository groupRepository;
    private final SubmissionRepository submissionRepository;
    private final IdObfuscator ids;

    // 1. Tạo bài tập
    @Transactional
    public ProblemResponse createProblem(ProblemRequest r) {
        if (problemrepository.existsBySlug(r.slug())) {
            throw new ConflictException("slug đã tồn tại");
        }
        Problem p = new Problem();
        apply(p, r);
        return toResponse(problemrepository.save(p));
    }

    // 2. CẬP NHẬT BÀI TẬP (Có so khớp và xóa test case mồ côi nếu có danh sách testCases)
    @Transactional
    public ProblemResponse update(String idH, ProblemRequest r) {

        Problem p = find(idH);

        if (!p.getSlug().equals(r.slug())
                && problemrepository.existsBySlug(r.slug())) {

            throw new ConflictException("Slug đã tồn tại");
        }

        apply(p, r);

        return toResponse(p);
    }

    // 3. LẤY DANH SÁCH BÀI TẬP
    @Transactional(readOnly = true)
    public List<ProblemResponse> list() {
        List<Problem> problems = problemrepository.findAll();
        if (problems.isEmpty()) return List.of();

        List<Long> idsList = problems.stream().map(Problem::getId).toList();
        Map<Long, Long> counts = toCountMap(testCaseRepository.countByProblemIds(idsList));

        return problems.stream()
                .map(p -> toResponse(p, counts.getOrDefault(p.getId(), 0L).intValue()))
                .toList();
    }

    // 4. LẤY CHI TIẾT BÀI TẬP
    @Transactional(readOnly = true)
    public ProblemResponse get(String idH) {
        return toResponse(find(idH));
    }

    // 5. XÓA BÀI TẬP
    @Transactional
    public void delete(String idH) {
        problemrepository.delete(find(idH));
    }

    // 6. LẤY DANH SÁCH TEST CASE
    @Transactional(readOnly = true)
    public List<TestCaseResponse> listTests(String problemIdH) {
        return testCaseRepository
                .findByProblemId(ids.decode(problemIdH))
                .stream()
                .map(this::toTc)
                .toList();
    }

    // 7. THÊM TEST CASE
    @Transactional
    public TestCaseResponse addTest(
            String problemIdH,
            TestCaseRequest r) {

        TestCase tc = new TestCase();
        tc.setProblem(find(problemIdH));
        applyTc(tc, r);

        return toTc(testCaseRepository.save(tc));
    }

    // 8. CẬP NHẬT TEST CASE
    @Transactional
    public TestCaseResponse updateTest(
            String problemIdH,
            String tcIdH,
            TestCaseRequest r) {

        TestCase tc = testCaseRepository
                .findByIdAndProblemId(
                        ids.decode(tcIdH),
                        ids.decode(problemIdH)
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy test case"
                        )
                );

        applyTc(tc, r);

        return toTc(tc);
    }

    // 9. XÓA TEST CASE
    @Transactional
    public void deleteTest(String problemIdH, String tcIdH) {

        TestCase tc = testCaseRepository
                .findByIdAndProblemId(
                        ids.decode(tcIdH),
                        ids.decode(problemIdH)
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy test case"
                        )
                );

        testCaseRepository.delete(tc);
    }

    // 10. THAY THẾ TOÀN BỘ TEST CASE (Xóa test case mồ côi)
    @Transactional
    public List<TestCaseResponse> replaceTests(String problemIdH, List<TestCaseItemRequest> requests) {
        Problem p = find(problemIdH);
        syncTestCases(p, requests != null ? requests : List.of());
        problemrepository.saveAndFlush(p);
        return listTests(problemIdH);
    }

    // 11. LẤY DANH SÁCH BÀI NỘP CHO ADMIN
    @Transactional(readOnly = true)
    public List<ProblemSubmissionResponse> listSubmissions(String problemIdH) {
        Long problemId = ids.decode(problemIdH);
        if (!problemrepository.existsById(problemId)) {
            throw new ResourceNotFoundException("Không tìm thấy bài tập");
        }
        return submissionRepository.findByProblemIdOrderByCreatedAtDesc(problemId)
                .stream()
                .map(this::toSubmissionResponse)
                .toList();
    }

    // 12. LẤY CHI TIẾT BÀI NỘP CHO ADMIN
    @Transactional(readOnly = true)
    public ProblemSubmissionDetailResponse getSubmissionDetail(String problemIdH, String submissionIdH) {
        Long problemId = ids.decode(problemIdH);
        Long submissionId = ids.decode(submissionIdH);

        Submission submission = submissionRepository.findByIdAndProblemId(submissionId, problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài nộp"));

        return toSubmissionDetailResponse(submission);
    }

    // TÌM PROBLEM THEO ID
    private Problem find(String idH) {
        return problemrepository.findById(ids.decode(idH))
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy bài tập"
                        )
                );
    }

    // ÁP DỤNG DỮ LIỆU REQUEST VÀO PROBLEM
    private void apply(Problem p, ProblemRequest r) {
        if (r.startAt() != null && r.deadline() != null && !r.deadline().isAfter(r.startAt())) {
            throw new BadRequestException(
                    "Deadline phải sau thời điểm bắt đầu"
            );
        }

        p.setTitle(r.title().trim());
        p.setSlug(r.slug());
        p.setContent(r.content());
        p.setDifficulty(r.difficulty());
        p.setTimeLimitMs(r.timeLimitMs());
        p.setMemoryLimitKb(r.memoryLimitKb());
        p.setPublished(r.published());
        p.setStartAt(r.startAt());
        p.setDeadline(r.deadline());
        p.setAllowLate(Boolean.TRUE.equals(r.allowLate()));
        p.setMaxAttempts(r.maxAttempts());
        p.getGroups().clear();  // xóa danh sách nhóm cũ
        if (r.groupIds() != null) {  // gán lại các nhóm mới
            for (String gh : r.groupIds()) {
                p.getGroups().add(
                        groupRepository.findById(ids.decode(gh))
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy Nhóm"
                                        ))
                );
            }
        }

        // Nếu request có gửi kèm danh sách testCases -> đồng bộ và loại bỏ test case mồ côi
        if (r.testCases() != null) {
            syncTestCases(p, r.testCases());
        }
    }

    // ĐỒNG BỘ TEST CASE VÀ DỌN DẸP RÁC TEST CASE MỒ CÔI
    private void syncTestCases(Problem p, List<TestCaseItemRequest> requests) {
        Set<Long> keptIds = new HashSet<>();
        Map<Long, TestCaseItemRequest> updateMap = new HashMap<>();
        List<TestCaseItemRequest> newCases = new ArrayList<>();

        for (TestCaseItemRequest req : requests) {
            if (req.id() != null && !req.id().isBlank()) {
                try {
                    Long tcId = ids.decode(req.id().trim());
                    keptIds.add(tcId);
                    updateMap.put(tcId, req);
                } catch (Exception e) {
                    newCases.add(req);
                }
            } else {
                newCases.add(req);
            }
        }

        // Xóa các test case cũ không còn trong danh sách (orphan removal)
        p.getTestCases().removeIf(tc -> tc.getId() != null && !keptIds.contains(tc.getId()));

        // Cập nhật các test case đang giữ lại
        for (TestCase tc : p.getTestCases()) {
            TestCaseItemRequest req = updateMap.get(tc.getId());
            if (req != null) {
                tc.setInputData(req.inputData());
                tc.setExpectedOutput(req.expectedOutput());
                tc.setHidden(req.hidden());
                tc.setScoreWeight(req.scoreWeight());
            }
        }

        // Thêm các test case mới
        for (TestCaseItemRequest req : newCases) {
            TestCase tc = new TestCase();
            tc.setProblem(p);
            tc.setInputData(req.inputData());
            tc.setExpectedOutput(req.expectedOutput());
            tc.setHidden(req.hidden());
            tc.setScoreWeight(req.scoreWeight());
            p.getTestCases().add(tc);
        }
    }

    private void applyTc(TestCase tc, TestCaseRequest r) {
        tc.setInputData(r.inputData());
        tc.setExpectedOutput(r.expectedOutput());
        tc.setHidden(r.hidden());
        tc.setScoreWeight(r.scoreWeight());
    }

    private Map<Long, Long> toCountMap(List<Object[]> rows) {
        Map<Long, Long> map = new java.util.HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return map;
    }

    private ProblemResponse toResponse(Problem p) {
        int count = p.getId() != null ? (int) testCaseRepository.countByProblemId(p.getId()) : 0;
        return toResponse(p, count);
    }

    private ProblemResponse toResponse(Problem p, int testCaseCount) {
        return new ProblemResponse(
                ids.encode(p.getId()),
                p.getTitle(),
                p.getSlug(),
                p.getContent(),
                p.getDifficulty(),
                p.getTimeLimitMs(),
                p.getMemoryLimitKb(),
                p.isPublished(),
                p.getStartAt(),
                p.getDeadline(),
                Boolean.TRUE.equals(p.getAllowLate()),
                p.getMaxAttempts(),
                p.getGroups()
                        .stream()
                        .map(g -> ids.encode(g.getId()))
                        .toList(),
                testCaseCount
        );
    }

    private TestCaseResponse toTc(TestCase t) {
        return new TestCaseResponse(
                ids.encode(t.getId()),
                t.getInputData(),
                t.getExpectedOutput(),
                t.isHidden(),
                t.getScoreWeight()
        );
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

    private ProblemSubmissionDetailResponse toSubmissionDetailResponse(Submission s) {
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
                        .errorMessage(tr.getErrorMessage())
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
