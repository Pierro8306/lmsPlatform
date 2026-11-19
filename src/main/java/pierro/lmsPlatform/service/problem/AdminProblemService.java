package pierro.lmsPlatform.service.problem;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pierro.lmsPlatform.dto.request.problem.ProblemRequest;
import pierro.lmsPlatform.dto.request.problem.TestCaseRequest;
import pierro.lmsPlatform.dto.response.problem.ProblemResponse;
import pierro.lmsPlatform.dto.response.problem.TestCaseResponse;

import pierro.lmsPlatform.entity.problem.Problem;
import pierro.lmsPlatform.entity.problem.TestCase;

import pierro.lmsPlatform.repository.problem.ProblemRepository;
import pierro.lmsPlatform.repository.problem.TestCaseRepository;
import pierro.lmsPlatform.repository.auth.UserGroupRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.BadRequestException;
import pierro.lmsPlatform.security.exception.ConflictException;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProblemService {
    private final ProblemRepository problemrepository;
    private final TestCaseRepository testCaseRepository;
    private final UserGroupRepository groupRepository;
    private final IdObfuscator ids;

    // Tạo bài tập
    @Transactional
    public ProblemResponse createProblem(ProblemRequest r) {
        if (problemrepository.existsBySlug(r.slug())) {
            throw new ConflictException("slug đã tồn tại");
        }
        Problem p = new Problem();
        apply(p, r);
        return toResponse(problemrepository.save(p));
    }

    // 2. CẬP NHẬT BÀI TẬP
    // ==========================================
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

    //==========================================
    // 3. LẤY DANH SÁCH BÀI TẬP
    // ==========================================

    @Transactional(readOnly = true)
    public List<ProblemResponse> list() {

        return problemrepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ==========================================
    // 4. LẤY CHI TIẾT BÀI TẬP
    // ==========================================

    @Transactional(readOnly = true)
    public ProblemResponse get(String idH) {

        return toResponse(find(idH));
    }

    // ==========================================
    // 5. XÓA BÀI TẬP
    // ==========================================

    @Transactional
    public void delete(String idH) {

        problemrepository.delete(find(idH));
    }

    // ==========================================
    // 6. LẤY DANH SÁCH TEST CASE
    // ==========================================

    @Transactional(readOnly = true)
    public List<TestCaseResponse> listTests(String problemIdH) {

        return testCaseRepository
                .findByProblemId(ids.decode(problemIdH))
                .stream()
                .map(this::toTc)
                .toList();
    }

    // ==========================================
    // 7. THÊM TEST CASE
    // ==========================================

    @Transactional
    public TestCaseResponse addTest(
            String problemIdH,
            TestCaseRequest r) {

        TestCase tc = new TestCase();

        tc.setProblem(find(problemIdH));

        applyTc(tc, r);

        return toTc(testCaseRepository.save(tc));
    }

    // ==========================================
    // 8. CẬP NHẬT TEST CASE
    // ==========================================

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

    // ==========================================
    // 9. XÓA TEST CASE
    // ==========================================

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

    // ==========================================
    // 10. TÌM PROBLEM THEO ID
    // ==========================================

    private Problem find(String idH) {

        return problemrepository.findById(ids.decode(idH))
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy bài tập"
                        )
                );
    }

    //ÁP DỤNG DỮ LIỆU REQUEST VÀO PROBLEM
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


    }

    // ==========================================
    // 12. ÁP DỤNG DỮ LIỆU REQUEST VÀO TEST CASE
    // ==========================================

    private void applyTc(TestCase tc, TestCaseRequest r) {

        tc.setInputData(r.inputData());
        tc.setExpectedOutput(r.expectedOutput());
        tc.setHidden(r.hidden());
        tc.setScoreWeight(r.scoreWeight());
    }

    //CHUYỂN PROBLEM THÀNH PROBLEM RESPONSE
    private ProblemResponse toResponse(Problem p) {

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

                p.getTestCases().size()
        );
    }

    // ==========================================
    // 14. CHUYỂN TEST CASE THÀNH TEST CASE RESPONSE
    // ==========================================

    private TestCaseResponse toTc(TestCase t) {

        return new TestCaseResponse(

                ids.encode(t.getId()),

                t.getInputData(),
                t.getExpectedOutput(),

                t.isHidden(),
                t.getScoreWeight()
        );
    }

}
