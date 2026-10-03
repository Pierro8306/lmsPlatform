package pierro.lmsPlatform.Service.Auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Gửi mail bất đồng bộ để không chặn request (và không để lộ email có tồn tại hay không qua thời gian phản hồi).
 * JavaMailSender chỉ có khi cấu hình spring.mail.host; chưa cấu hình thì chỉ ghi log cảnh báo.
 */
@Slf4j
@Service
public class MailService {
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;

    public MailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                       @Value("${app.mail.from:${spring.mail.username:no-reply@lms.local}}") String from) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
    }

    @Async
    public void sendPasswordReset(String to, String resetLink, long validMinutes) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("Chưa cấu hình spring.mail.* nên không gửi được email đặt lại mật khẩu");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Đặt lại mật khẩu tài khoản LMS");
            message.setText("""
                    Xin chào,

                    Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.
                    Nhấn vào liên kết dưới đây để đặt mật khẩu mới (có hiệu lực trong %d phút, chỉ dùng được một lần):

                    %s

                    Nếu bạn không yêu cầu, hãy bỏ qua email này — mật khẩu của bạn vẫn an toàn.
                    """.formatted(validMinutes, resetLink));
            sender.send(message);
        } catch (MailException e) {
            log.error("Gửi email đặt lại mật khẩu thất bại", e);
        }
    }
}
