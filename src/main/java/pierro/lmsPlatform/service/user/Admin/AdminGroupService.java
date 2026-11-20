package pierro.lmsPlatform.service.user.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.entity.auth.User;
import pierro.lmsPlatform.entity.auth.UserGroup;
import pierro.lmsPlatform.repository.auth.UserGroupRepository;
import pierro.lmsPlatform.repository.auth.UserRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.BadRequestException;
import pierro.lmsPlatform.security.exception.ConflictException;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminGroupService {

    private final UserGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final IdObfuscator ids;

    @Transactional(readOnly = true)
    public List<Map<String, String>> list() {
        return groupRepository.findAll()
                .stream()
                .map(g -> Map.of(
                        "id", ids.encode(g.getId()),
                        "name", g.getName()
                ))
                .toList();
    }

    @Transactional
    public Map<String, String> create(String rawName) {
        if (rawName == null || rawName.trim().isBlank()) {
            throw new BadRequestException("Tên nhóm không được để trống");
        }
        String name = rawName.trim();

        if (groupRepository.existsByName(name)) {
            throw new ConflictException("Tên nhóm đã tồn tại");
        }

        UserGroup g = new UserGroup();
        g.setName(name);

        try {
            groupRepository.saveAndFlush(g);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Tên nhóm đã tồn tại");
        }

        return Map.of(
                "id", ids.encode(g.getId()),
                "name", g.getName()
        );
    }

    @Transactional
    public Map<String, String> rename(String groupIdH, String rawName) {
        if (rawName == null || rawName.trim().isBlank()) {
            throw new BadRequestException("Tên nhóm không được để trống");
        }
        String name = rawName.trim();
        Long groupId = ids.decode(groupIdH);

        UserGroup g = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm"));

        if (groupRepository.existsByNameAndIdNot(name, groupId)) {
            throw new ConflictException("Tên nhóm đã tồn tại");
        }

        g.setName(name);
        try {
            groupRepository.saveAndFlush(g);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Tên nhóm đã tồn tại");
        }

        return Map.of(
                "id", ids.encode(g.getId()),
                "name", g.getName()
        );
    }

    @Transactional
    public void delete(String groupIdH) {
        Long groupId = ids.decode(groupIdH);
        UserGroup g = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm"));

        List<User> members = userRepository.findByUserGroup_Id(groupId);
        for (User u : members) {
            u.setUserGroup(null);
        }
        userRepository.saveAll(members);

        try {
            groupRepository.delete(g);
            groupRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Nhóm đang được sử dụng trong bài tập hoặc bài thi, không thể xoá");
        }
    }

    @Transactional
    public void setMembers(String groupIdH, List<String> memberIdsH) {
        Long groupId = ids.decode(groupIdH);
        UserGroup g = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm"));

        List<String> userIdsH = memberIdsH != null ? memberIdsH : List.of();

        List<Long> userIds = userIdsH.stream()
                .map(ids::decode)
                .toList();

        Set<Long> uniqueIds = new HashSet<>(userIds);
        if (uniqueIds.size() != userIds.size()) {
            throw new BadRequestException("Danh sách ID thành viên bị trùng");
        }

        List<User> newMembers = userIds.isEmpty() ? List.of() : userRepository.findAllById(userIds);
        if (newMembers.size() != userIds.size()) {
            throw new ResourceNotFoundException("Có tài khoản không tồn tại");
        }

        List<User> oldMembers = userRepository.findByUserGroup_Id(g.getId());
        for (User user : oldMembers) {
            if (!uniqueIds.contains(user.getId())) {
                user.setUserGroup(null);
            }
        }
        for (User user : newMembers) {
            user.setUserGroup(g);
        }
    }
}
