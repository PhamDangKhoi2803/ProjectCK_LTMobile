package ute.nhom27.chatserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ute.nhom27.chatserver.dto.GroupMemberDTO;
import ute.nhom27.chatserver.entity.ChatGroup;
import ute.nhom27.chatserver.entity.GroupMember;
import ute.nhom27.chatserver.repository.GroupMemberRepository;
import ute.nhom27.chatserver.service.IGroupService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    @Autowired
    private IGroupService groupService;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    // Tạo nhóm chat
    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestParam String name, @RequestParam Long ownerId) {
        ChatGroup group = groupService.createGroup(name, ownerId);
        if (group != null) {
            return ResponseEntity.ok(Map.of(
                "message", "Tạo nhóm thành công",
                "group", group
            ));
        }
        return ResponseEntity.badRequest().body(Map.of(
            "message", "Không thể tạo nhóm. Vui lòng thử lại"
        ));
    }

    // Thêm thành viên vào nhóm
    @PostMapping("/{groupId}/members/add")
    public ResponseEntity<?> addMember(@PathVariable Long groupId, @RequestParam Long userId) {
        boolean success = groupService.addMember(groupId, userId);
        if (success) {
            return ResponseEntity.ok(Map.of(
                "message", "Đã thêm thành viên vào nhóm"
            ));
        }
        return ResponseEntity.badRequest().body(Map.of(
            "message", "Không thể thêm thành viên. Người dùng có thể đã là thành viên của nhóm"
        ));
    }

    // Xóa thành viên khỏi nhóm hoặc xóa cả nhóm nếu là admin
    @DeleteMapping("/{groupId}/members/remove")
    public ResponseEntity<?> removeMember(
            @PathVariable Long groupId,
            @RequestParam Long userId,
            @RequestParam(required = false) Boolean deleteGroup) {

        System.out.println("Processing request: groupId=" + groupId + ", userId=" + userId + ", deleteGroup=" + deleteGroup);

        GroupMember member = groupMemberRepository.findByUserIdAndChatGroupId(userId, groupId);
        System.out.println("Found member: " + (member != null ? member.getRole() : "null"));

        // Nếu yêu cầu xóa nhóm và user là admin
        if (deleteGroup != null && deleteGroup && member != null && "admin".equals(member.getRole())) {
            boolean success = groupService.deleteGroup(groupId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "message", "Nhóm đã được xóa thành công"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Không thể xóa nhóm"
                ));
            }
        } else {
            // Giữ nguyên chức năng xóa thành viên
            boolean success = groupService.removeMember(groupId, userId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "message", "Đã xóa thành viên khỏi nhóm"
                ));
            }
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Không thể xóa thành viên khỏi nhóm"
            ));
        }
    }

    // Lấy danh sách thành viên
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberDTO>> getMembers(@PathVariable Long groupId) {
        List<GroupMemberDTO> members = groupService.getGroupMembersWithInfo(groupId);
        if (members != null) {
            return ResponseEntity.ok(members);
        }
        return ResponseEntity.badRequest().build();
    }

    // Lấy thông tin nhóm
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupInfo(@PathVariable Long groupId) {
        ChatGroup group = groupService.getGroupById(groupId);
        if (group != null) {
            return ResponseEntity.ok(Map.of(
                "message", "Lấy thông tin nhóm thành công",
                "group", group
            ));
        }
        return ResponseEntity.badRequest().body(Map.of(
            "message", "Không tìm thấy nhóm"
        ));
    }

    // Lấy danh sách nhóm của user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserGroups(@PathVariable Long userId) {
        List<ChatGroup> groups = groupService.getGroupsByUserId(userId);
        if (groups != null) {
            return ResponseEntity.ok(Map.of(
                "message", "Lấy danh sách nhóm thành công",
                "groups", groups
            ));
        }
        return ResponseEntity.badRequest().body(Map.of(
            "message", "Không thể lấy danh sách nhóm"
        ));
    }
}
