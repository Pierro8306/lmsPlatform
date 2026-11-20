package pierro.lmsPlatform.dto.response.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pierro.lmsPlatform.entity.problem.Verdict;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSubmissionDetailResponse {
    private String id;
    private String problemId;
    private String problemTitle;
    private String userId;
    private String username;
    private String language;
    private String sourceCode;
    private Verdict status;
    private Integer execTimeMs;
    private Integer memoryUsedKb;
    private LocalDateTime createdAt;
    private int totalTests;
    private int passedTests;
    private List<SubmissionTestResultDto> testResults;
}
