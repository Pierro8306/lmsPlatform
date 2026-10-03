package pierro.lmsPlatform.Entity.Quiz;

public enum AttemptStatus {
    IN_PROGRESS,   // đã bắt đầu, chưa nộp
    SUBMITTED,     // đã nộp đúng hạn và được chấm
    EXPIRED        // quá giờ, không được ghi nhận
}
