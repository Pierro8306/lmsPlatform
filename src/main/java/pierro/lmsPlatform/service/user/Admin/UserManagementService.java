package pierro.lmsPlatform.service.user.Admin;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import pierro.lmsPlatform.dto.request.Auth.CreateUserDto;
import pierro.lmsPlatform.dto.request.Auth.UpdateUserRequest;
import pierro.lmsPlatform.dto.response.UserResponseDto;
import pierro.lmsPlatform.entity.auth.Role;
import pierro.lmsPlatform.entity.auth.User;
import pierro.lmsPlatform.entity.auth.UserGroup;
import pierro.lmsPlatform.repository.auth.PasswordResetTokenRepository;
import pierro.lmsPlatform.repository.auth.RoleRepository;
import pierro.lmsPlatform.repository.auth.UserGroupRepository;
import pierro.lmsPlatform.repository.auth.UserRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.ConflictException;
import pierro.lmsPlatform.security.exception.ForbiddenException;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private static final String ADMIN_ROLE = "ADMIN";
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final IdObfuscator idObfuscator;

    @Transactional
    public void createUser(CreateUserDto dto) {
        String username = dto.getUsername().trim();
        String email = normalizeEmail(dto.getEmail());
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username đã tồn tại");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email đã được sử dụng");
        }
        User user = new User();
        user.setRole(findRole(dto.getRole()));
        user.setUserGroup(findGroup(dto.getUserGroup()));
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String idHash, Long currentAdminId) {
        Long userId = idObfuscator.decode(idHash);
        if (userId.equals(currentAdminId)) {
            throw new ForbiddenException("Không thể tự xoá tài khoản của chính mình");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (hasRole(user, ADMIN_ROLE)) {
            assertNotLastAdmin(userId);
        }

        try {
            passwordResetTokenRepository.deleteByUser_Id(userId);
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Người dùng đã có bài nộp/bài thi/sự kiện liên quan nên không thể xoá");
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUser() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getByRole(String roleName) {
        findRole(roleName); // 404 nếu role không tồn tại
        return userRepository.findByRole_Name(roleName).stream().map(this::toDto).toList();
    }

    @Transactional
    public void updateUser(UpdateUserRequest req, Long currentAdminId) {
        Long userId = idObfuscator.decode(req.getId());
        String username = req.getUsername().trim();
        String email = normalizeEmail(req.getEmail());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (userRepository.existsByUsernameAndIdNot(username, userId)) {
            throw new ConflictException("Username đã tồn tại");
        }
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId)) {
            throw new ConflictException("Email đã được sử dụng");
        }
        Role newRole = findRole(req.getRole());
        boolean roleChanged = user.getRole() == null || !user.getRole().getId().equals(newRole.getId());
        if (roleChanged) {
            if (userId.equals(currentAdminId)) {
                throw new ForbiddenException("Không thể tự thay đổi vai trò của chính mình");
            }
            if (hasRole(user, ADMIN_ROLE)) {
                assertNotLastAdmin(userId);   // đang hạ quyền một admin
            }
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setRole(newRole);
        user.setUserGroup(findGroup(req.getUserGroup()));
        // user đang được quản lý trong transaction nên không cần save()
    }

    /** Email luôn lưu dạng chữ thường, đã trim: tránh "A@x.com" và "a@x.com" thành hai tài khoản. */
    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRole() != null && roleName.equals(user.getRole().getName());
    }

    /**
     * Đảm bảo hệ thống luôn còn ít nhất một ADMIN sau khi xoá/hạ quyền {@code targetAdminId}.
     * Khoá toàn bộ dòng admin trước khi đếm để hai request đồng thời không thể cùng vượt qua kiểm tra.
     */
    private void assertNotLastAdmin(Long targetAdminId) {
        List<User> admins = userRepository.lockAllByRoleName(ADMIN_ROLE);
        boolean otherAdminExists = admins.stream().anyMatch(a -> !a.getId().equals(targetAdminId));
        if (!otherAdminExists) {
            throw new ConflictException("Hệ thống phải còn ít nhất một tài khoản ADMIN");
        }
    }

    private Role findRole(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role: " + name));
    }

    private UserGroup findGroup(String name) {
        if (!StringUtils.hasText(name)) return null;
        return userGroupRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm: " + name));
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

