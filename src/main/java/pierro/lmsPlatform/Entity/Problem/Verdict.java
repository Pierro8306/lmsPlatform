package pierro.lmsPlatform.Entity.Problem;

public enum Verdict {
    AC,   // Accepted
    WA,   // Wrong Answer
    TLE,  // Time Limit Exceeded
    MLE,  // Memory Limit Exceeded
    RTE,  // Runtime Error
    OLE,  // Output Limit Exceeded
    CE,   // Compile Error
    SE    // System Error (lỗi phía judge: docker chết, thiếu image, ...)
}
