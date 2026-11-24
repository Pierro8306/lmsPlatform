package pierro.lmsPlatform.service.problem.CodeReview.tools;

public class SkippedException extends RuntimeException {

    public SkippedException(String m) {
        super(m);
    }
}