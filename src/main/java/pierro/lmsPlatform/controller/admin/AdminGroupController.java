package pierro.lmsPlatform.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.dto.response.ResponseObject;
import pierro.lmsPlatform.entity.auth.User;
import pierro.lmsPlatform.entity.auth.UserGroup;
import pierro.lmsPlatform.repository.auth.UserGroupRepository;
import pierro.lmsPlatform.repository.auth.UserRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.ConflictException;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/groups")
@RequiredArgsConstructor

public class AdminGroupController {
    private final UserGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final IdObfuscator ids;

    // DTO tạo nhóm
    public record GroupRequest(@NotBlank String name) {}
    // DTO cập nhật thành viên nhóm
    public record MembersRequest(@NotEmpty List<String> userIds) {}
    // 1. Lấy danh sách nhóm
    @GetMapping
    public ResponseEntity<ResponseObject> list() {

        var data = groupRepository.findAll()
                .stream()
                .map(g -> Map.of(
                        "id", ids.encode(g.getId()),
                        "name", g.getName()
                ))
                .toList();

        return ResponseObject.ok(
                "Lấy danh sách nhóm thành công",
                data
        );
    }

    // 2. Tạo nhóm mới
    @PostMapping
    public ResponseEntity<ResponseObject> create(
            @Valid @RequestBody GroupRequest r
    ) {

        String name = r.name().trim();

        if (name.isBlank()) {
            throw new IllegalArgumentException("Tên nhóm không được để trống");
        }

        if (groupRepository.findByName(name).isPresent()) {
            throw new ConflictException("Tên nhóm đã tồn tại");
        }

        UserGroup g = new UserGroup();
        g.setName(name);

        try {
            groupRepository.saveAndFlush(g);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Tên nhóm đã tồn tại");
        }

        return ResponseObject.ok(
                "Tạo nhóm thành công",
                Map.of(
                        "id", ids.encode(g.getId()),
                        "name", g.getName()
                )
        );
    }

    // 3. Cập nhật toàn bộ thành viên của nhóm
    @PutMapping("/{groupId}/members")
    @Transactional
    public ResponseEntity<ResponseObject> setMembers(
            @PathVariable String groupId,
            @Valid @RequestBody MembersRequest r
    ) {

        // Tìm nhóm
        UserGroup g = groupRepository.findById(ids.decode(groupId))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy nhóm")
                );

        // Giải mã ID user
        List<Long> userIds = r.userIds()
                .stream()
                .map(ids::decode)
                .toList();

        // Kiểm tra ID trùng
        Set<Long> uniqueIds = new HashSet<>(userIds);

        if (uniqueIds.size() != userIds.size()) {
            throw new IllegalArgumentException(
                    "Danh sách ID thành viên bị trùng"
            );
        }

        // Lấy danh sách user mới
        List<User> newMembers = userRepository.findAllById(userIds);

        // Kiểm tra ID không tồn tại
        if (newMembers.size() != userIds.size()) {
            throw new ResourceNotFoundException(
                    "Có tài khoản không tồn tại"
            );
        }

        // Lấy thành viên hiện tại của nhóm
        List<User> oldMembers =
                userRepository.findByUserGroup_Id(g.getId());

        // Gỡ thành viên không còn trong danh sách mới
        for (User user : oldMembers) {
            if (!uniqueIds.contains(user.getId())) {
                user.setUserGroup(null);
            }
        }

        // Gán thành viên mới vào nhóm
        for (User user : newMembers) {
            user.setUserGroup(g);
        }

        return ResponseObject.ok(
                "Cập nhật thành viên nhóm thành công"
        );
    }
}
