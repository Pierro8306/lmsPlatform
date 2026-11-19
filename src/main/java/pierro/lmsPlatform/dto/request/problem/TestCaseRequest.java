package pierro.lmsPlatform.dto.request.problem;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TestCaseRequest(String inputData,
                              @NotNull
                              String expectedOutput,
                              boolean hidden,
                              @PositiveOrZero
                              double scoreWeight) {
}
