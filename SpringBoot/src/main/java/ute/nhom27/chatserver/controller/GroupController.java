package ute.nhom27.chatserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ute.nhom27.chatserver.entity.GroupMember;
import ute.nhom27.chatserver.service.IGroupService;

import java.util.List;

@RestController
@RequestMapping("/api/chatrooms")
public class GroupController {
    @Autowired
    private IGroupService groupService;

    // Tạo Chat nhóm
    @PostMapping("/create")
    public boolean createGroupChat(@RequestParam String name, @RequestParam Long ownerId) {
        return groupService.createGroup(name, ownerId);
    }

    // Thêm thành viên vào ChatRoom
    @PostMapping("/{chatRoomId}/add-member")
    public boolean addMember(@PathVariable Long groupId, @RequestParam Long userId) {
        return groupService.addMember(groupId, userId);
    }

    // Xoá thành viên khỏi ChatRoom
    @DeleteMapping("/{chatRoomId}/remove-member")
    public boolean removeMember(@PathVariable Long groupId, @RequestParam Long userId) {
        return groupService.removeMember(groupId, userId);
    }

    // Lấy danh sách thành viên
    @GetMapping("/{chatRoomId}/members")
    public List<GroupMember> getMembers(@PathVariable Long groupId) {
        return groupService.getGroupMembers(groupId);
    }
}
