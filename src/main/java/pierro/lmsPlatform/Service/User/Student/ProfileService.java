package pierro.lmsPlatform.Service.User.Student;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.DTO.Request.Auth.UpdateProfileRequest;
import pierro.lmsPlatform.DTO.Response.UserResposeDto;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Repository.Auth.UserRepository;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;
import pierro.lmsPlatform.Security.Exception.ConflictException;
import pierro.lmsPlatform.Security.Exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final IdObfuscator idObfuscator;

    @Transactional(readOnly = true)
    public UserResposeDto getProfile(Long userId) {
        return toDto(findUser(userId));
    }

    @Transactional
    public UserResposeDto updateProfile(Long userId, UpdateProfileRequest req) {
        User user = findUser(userId);
        String username = req.getUsername().trim();
        String email = req.getEmail().trim();

        if (userRepository.existsByUsernameAndIdNot(username, userId)) {
            throw new ConflictException("Username đã tồn tại");
        }
        if (userRepository.existsByEmailAndIdNot(email, userId)) {
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

    private UserResposeDto toDto(User u) {
        return new UserResposeDto(
                idObfuscator.encode(u.getId()),
                u.getRole() != null ? u.getRole().getName() : null,
                u.getUserGroup() != null ? u.getUserGroup().getName() : null,
                u.getUsername(),
                u.getEmail());
    }
}