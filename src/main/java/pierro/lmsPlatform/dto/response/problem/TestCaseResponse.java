package pierro.lmsPlatform.dto.response.problem;

public record TestCaseResponse(String id,
                               String inputData,
                               String expectedOutput,
                               boolean hidden,
                               double scoreWeight) {
}
