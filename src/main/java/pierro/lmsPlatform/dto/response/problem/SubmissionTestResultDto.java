package pierro.lmsPlatform.dto.response.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pierro.lmsPlatform.entity.problem.Verdict;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionTestResultDto {
    private String id;
    private String testCaseId;
    private Verdict status;
    private Integer execTimeMs;
    private Integer memoryUsedKb;
    private String errorMessage;
    private boolean hidden;
}
