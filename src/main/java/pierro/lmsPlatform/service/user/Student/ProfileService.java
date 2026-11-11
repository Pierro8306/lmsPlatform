package pierro.lmsPlatform.service.user.Student;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.dto.request.Auth.UpdateProfileRequest;
import pierro.lmsPlatform.dto.response.UserResponseDto;
import pierro.lmsPlatform.entity.auth.User;
import pierro.lmsPlatform.repository.auth.UserRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.ConflictException;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final IdObfuscator idObfuscator;

    @Transactional(readOnly = true)
    public UserResponseDto getProfile(Long userId) {
        return toDto(findUser(userId));
    }

    @Transactional
    public UserResponseDto updateProfile(Long userId, UpdateProfileRequest req) {
        User user = findUser(userId);
        String username = req.getUsername().trim();
        String email = req.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsernameAndIdNot(username, userId)) {
            throw new ConflictException("Username đã tồn tại");
        }
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId)) {
            throw new ConflictException("Email đã được sử dụng");
        }
        user.setUsername(username);
        user.setEmail(email);
        try {
            userRepository.flush();   // bắt lỗi trùng unique do 2 request đồng thời
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Username hoặc email đã được sử dụng");
        }
        return toDto(user);
    }

    private User findUser(Long id) {
        return userRepository.findWithRoleAndGroupById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    private UserResponseDto toDto(User u) {
        return new UserResponseDto(
                idObfuscator.encode(u.getId()),
                u.getRole() != null ? u.getRole().getName() : null,
                u.getUserGroup() != null ? u.getUserGroup().getName() : null,
                u.getUsername(),
                u.getEmail());
    }
}

