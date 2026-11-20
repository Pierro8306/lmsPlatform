package pierro.lmsPlatform.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.dto.response.ResponseObject;
import pierro.lmsPlatform.service.user.Admin.AdminGroupService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/groups")
@RequiredArgsConstructor
public class AdminGroupController {

    private final AdminGroupService groupService;

    // DTO tạo và sửa tên nhóm
    public record GroupRequest(@NotBlank(message = "Tên nhóm không được để trống") String name) {}

    // DTO cập nhật thành viên nhóm (cho phép rỗng để gỡ hết thành viên)
    public record MembersRequest(List<String> userIds) {}

    // 1. Lấy danh sách nhóm
    @GetMapping
    public ResponseEntity<ResponseObject> list() {
        return ResponseObject.ok(
                "Lấy danh sách nhóm thành công",
                groupService.list()
        );
    }

    // 2. Tạo nhóm mới
    @PostMapping
    public ResponseEntity<ResponseObject> create(@Valid @RequestBody GroupRequest r) {
        return ResponseObject.ok(
                "Tạo nhóm thành công",
                groupService.create(r.name())
        );
    }

    // 3. Đổi tên nhóm
    @PutMapping("/{groupId}")
    public ResponseEntity<ResponseObject> rename(
            @PathVariable String groupId,
            @Valid @RequestBody GroupRequest r
    ) {
        return ResponseObject.ok(
                "Đổi tên nhóm thành công",
                groupService.rename(groupId, r.name())
        );
    }

    // 4. Xóa nhóm
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ResponseObject> delete(@PathVariable String groupId) {
        groupService.delete(groupId);
        return ResponseObject.ok("Xoá nhóm thành công");
    }

    // 5. Cập nhật thành viên của nhóm
    @PutMapping("/{groupId}/members")
    public ResponseEntity<ResponseObject> setMembers(
            @PathVariable String groupId,
            @Valid @RequestBody MembersRequest r
    ) {
        groupService.setMembers(groupId, r.userIds());
        return ResponseObject.ok("Cập nhật thành viên nhóm thành công");
    }
}
