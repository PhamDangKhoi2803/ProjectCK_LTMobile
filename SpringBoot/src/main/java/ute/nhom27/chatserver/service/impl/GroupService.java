package ute.nhom27.chatserver.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ute.nhom27.chatserver.dto.GroupMemberDTO;
import ute.nhom27.chatserver.entity.ChatGroup;
import ute.nhom27.chatserver.entity.GroupMember;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.ChatGroupRepository;
import ute.nhom27.chatserver.repository.GroupMemberRepository;
import ute.nhom27.chatserver.repository.GroupMessageRepository;
import ute.nhom27.chatserver.repository.UserRepository;
import ute.nhom27.chatserver.service.IGroupService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService implements IGroupService {

    @Autowired
    private ChatGroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupMessageRepository groupMessageRepository;

    @Autowired
    private ChatGroupRepository chatGroupRepository;

    @Override
    public ChatGroup createGroup(String name, Long ownerId) {
        try {
            User owner = userRepository.findById(ownerId).orElseThrow();
            ChatGroup group = new ChatGroup();
            group.setName(name);
            group.setOwner(owner);
            group.setAvatarUrl(owner.getAvatarUrl());
            ChatGroup savedGroup = groupRepository.save(group);

            // Gán owner là admin của nhóm
            GroupMember adminMember = new GroupMember();
            adminMember.setUser(owner);
            adminMember.setChatGroup(savedGroup);
            adminMember.setRole("admin");
            adminMember.setJoinedAt(String.valueOf(LocalDateTime.now()));

            groupMemberRepository.save(adminMember);

            return savedGroup;
        } catch (Exception e) {
            return null;
        }
    }

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

    @Override
    public boolean removeMember(Long groupId, Long userId) {
        try {
            System.out.println("Attempting to remove member: userId=" + userId + " from groupId=" + groupId);

            // Kiểm tra xem member có tồn tại không
            boolean exists = groupMemberRepository.existsByChatGroupIdAndUserId(groupId, userId);
            System.out.println("Member exists: " + exists);

            if (!exists) {
                System.out.println("Cannot remove non-existing member");
                return false;
            }

            int result = groupMemberRepository.deleteByChatGroupIdAndUserId(groupId, userId);
            System.out.println("Deletion result: " + result + " records deleted");

            return result > 0;
        } catch (Exception e) {
            System.err.println("Error removing member: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<GroupMember> getGroupMembers(Long groupId) {
        try {
            return groupMemberRepository.findByChatGroupId(groupId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ChatGroup getGroupById(Long groupId) {
        try {
            Optional<ChatGroup> group = groupRepository.findById(groupId);
            return group.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<ChatGroup> getGroupsByUserId(Long userId) {
        try {
            return groupRepository.findGroupsByUserId(userId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<GroupMemberDTO> getGroupMembersWithInfo(Long groupId) {
        List<GroupMember> members = groupMemberRepository.findByChatGroupId(groupId);

        List<GroupMemberDTO> memberDTOs = new ArrayList<>();

        for (GroupMember member : members) {
            User user = userRepository.findById(member.getUser().getId()).orElse(null);
            if (user != null) {
                GroupMemberDTO dto = new GroupMemberDTO(
                        groupId,
                        user.getId(),
                        user.getUsername(),
                        member.getRole(),
                        user.getAvatarUrl()
                );
                memberDTOs.add(dto);
            }
        }

        return memberDTOs;
    }

    @Override
    @Transactional
    public boolean deleteGroup(Long groupId) {
        try {
            System.out.println("Starting delete group process for groupId: " + groupId);

            // Xóa tất cả tin nhắn của nhóm
            try {
                int messagesDeleted = groupMessageRepository.deleteAllByChatGroupId(groupId);
                System.out.println("Deleted " + messagesDeleted + " messages for groupId: " + groupId);
            } catch (Exception e) {
                System.err.println("Error deleting messages: " + e.getMessage());
                e.printStackTrace();
                return false;
            }

            // Xóa tất cả thành viên của nhóm
            try {
                int membersDeleted = groupMemberRepository.deleteAllByChatGroupId(groupId);
                System.out.println("Deleted " + membersDeleted + " members for groupId: " + groupId);
            } catch (Exception e) {
                System.err.println("Error deleting members: " + e.getMessage());
                e.printStackTrace();
                return false;
            }

            // Xóa nhóm
            try {
                chatGroupRepository.deleteById(groupId);
                System.out.println("Successfully deleted group with id: " + groupId);
            } catch (Exception e) {
                System.err.println("Error deleting group: " + e.getMessage());
                e.printStackTrace();
                return false;
            }

            return true;
        } catch (Exception e) {
            System.err.println("Unexpected error in deleteGroup method: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
