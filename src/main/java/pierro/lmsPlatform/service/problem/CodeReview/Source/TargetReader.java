package pierro.lmsPlatform.service.problem.CodeReview.Source;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pierro.lmsPlatform.entity.problem.Problem;
import pierro.lmsPlatform.entity.problem.Submission;
import pierro.lmsPlatform.repository.problem.SubmissionRepository;
import pierro.lmsPlatform.repository.project.ProjectSubmissionRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;

@Service
@RequiredArgsConstructor
public class TargetReader {

    public record ProblemInfo(String language, String sourceCode, String title, String statement) {}
    public record ProjectInfo(String githubUrl, String title, String description) {}

    private final IdObfuscator idObfuscator;
    private final SubmissionRepository submissions;
    private final ProjectSubmissionRepository projectSubmissions;

    @Transactional(readOnly = true)
    public ProblemInfo problem(String submissionId) {
        Submission s = submissions.findById(idObfuscator.decode(submissionId))
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        Problem p = s.getProblem();
        return new ProblemInfo(s.getLanguage(), s.getSourceCode(), p.getTitle(), p.getContent());
    }
}