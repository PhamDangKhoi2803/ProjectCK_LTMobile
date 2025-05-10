package ute.nhom27.chatserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ute.nhom27.chatserver.entity.ChatGroup;
import ute.nhom27.chatserver.entity.GroupMember;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.ChatGroupRepository;
import ute.nhom27.chatserver.repository.GroupMemberRepository;
import ute.nhom27.chatserver.repository.UserRepository;
import ute.nhom27.chatserver.service.IGroupService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupService implements IGroupService {

    @Autowired
    private ChatGroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    // Tạo nhóm
    @Override
    public boolean createGroup(String name, Long ownerId) {
        try {
            User owner = userRepository.findById(ownerId).orElseThrow();
            ChatGroup group = new ChatGroup();
            group.setName(name);
            group.setOwner(owner);
            groupRepository.save(group);

            // Gán owner là admin của nhóm
            GroupMember adminMember = new GroupMember();
            adminMember.setUser(owner);
            adminMember.setChatGroup(group);
            adminMember.setRole("admin");
            adminMember.setJoinedAt(String.valueOf(LocalDateTime.now()));

            groupMemberRepository.save(adminMember);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Thêm thành viên vào nhóm
    @Override
    public boolean addMember(Long groupId, Long userId) {
        try {
            ChatGroup group = groupRepository.findById(groupId).orElseThrow();
            User user = userRepository.findById(userId).orElseThrow();

            boolean exists = groupMemberRepository.existsByChatGroupIdAndUserId(groupId, userId);
            if (exists) {
                return false;
            }

            GroupMember member = new GroupMember();
            member.setUser(user);
            member.setChatGroup(group);
            member.setRole("member");
            member.setJoinedAt(String.valueOf(LocalDateTime.now()));

            groupMemberRepository.save(member);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Xóa thành viên khỏi nhóm
    @Override
    public boolean removeMember(Long groupId, Long userId) {
        try {
            groupMemberRepository.deleteByChatGroupIdAndUserId(groupId, userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<GroupMember> getGroupMembers(Long groupId) {
        return groupMemberRepository.findByChatGroupId(groupId);
    }
}
