package pierro.lmsPlatform.service.quiz.Student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.entity.quiz.Attempt;
import pierro.lmsPlatform.entity.quiz.AttemptStatus;
import pierro.lmsPlatform.repository.quiz.AttemptRepository;
import pierro.lmsPlatform.service.quiz.QuizHelper;

import java.time.LocalDateTime;

/** Đóng các lượt làm bài bị bỏ dở (quá hạn mà học viên không quay lại nộp) để bảng điểm không hiện IN_PROGRESS mãi. */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizMaintenanceService {

    private final AttemptRepository attemptRepository;

    @Scheduled(initialDelay = 30_000, fixedDelayString = "${app.quiz.expire-scan-ms:60000}")
    @Transactional
    public void expireAbandonedAttempts() {
        LocalDateTime now = LocalDateTime.now();
        int expired = 0;
        for (Attempt a : attemptRepository.findByStatus(AttemptStatus.IN_PROGRESS)) {
            if (QuizHelper.isPastDeadline(a, a.getQuiz(), now)) {
                QuizHelper.expire(a, now);
                expired++;
            }
        }
        if (expired > 0) {
            log.info("Đã đóng {} lượt làm bài quá hạn", expired);
        }
    }
}
