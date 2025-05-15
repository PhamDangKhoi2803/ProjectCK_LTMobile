package ute.nhom27.chatserver.service;

import ute.nhom27.chatserver.entity.ChatGroup;
import ute.nhom27.chatserver.entity.GroupMember;

import java.util.List;

public interface IGroupService {
    // Tạo nhóm
    ChatGroup createGroup(String name, Long ownerId);

    // Thêm thành viên vào nhóm
    boolean addMember(Long groupId, Long userId);

    // Xóa thành viên khỏi nhóm
    boolean removeMember(Long groupId, Long userId);

    List<GroupMember> getGroupMembers(Long groupId);

    ChatGroup getGroupById(Long groupId);

    List<ChatGroup> getGroupsByUserId(Long userId);
}
