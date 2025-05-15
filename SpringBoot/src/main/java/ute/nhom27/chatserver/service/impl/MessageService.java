package ute.nhom27.chatserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ute.nhom27.chatserver.dto.MessageListDTO;
import ute.nhom27.chatserver.entity.ChatGroup;
import ute.nhom27.chatserver.entity.ChatMessage;
import ute.nhom27.chatserver.entity.Friendship;
import ute.nhom27.chatserver.entity.GroupMessage;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.ChatGroupRepository;
import ute.nhom27.chatserver.repository.ChatMessageRepository;
import ute.nhom27.chatserver.repository.FriendshipRepository;
import ute.nhom27.chatserver.repository.GroupMessageRepository;
import ute.nhom27.chatserver.service.IMessageService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService implements IMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private GroupMessageRepository groupMessageRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private ChatGroupRepository chatGroupRepository;

    @Override
    public List<ChatMessage> getPrivateMessages(Long user1Id, Long user2Id) {
        return chatMessageRepository.findAllMessagesBetweenUsers(user1Id, user2Id);
    }

    @Override
    public List<GroupMessage> getGroupMessages(Long groupId) {
        return groupMessageRepository.findByChatGroupIdOrderByTimestampAsc(groupId);
    }

    @Override
    public List<MessageListDTO> getGroupLastMessages(Long userId) {
        // Lấy danh sách nhóm mà user tham gia
        List<ChatGroup> userGroups = chatGroupRepository.findGroupsByUserId(userId);

        return userGroups.stream().map(group -> {
            // Lấy tin nhắn cuối cùng của nhóm
            GroupMessage lastMessage = groupMessageRepository.findLastMessageByGroupId(group.getId());

            // Đếm số tin nhắn chưa đọc
            int unreadCount = groupMessageRepository.countUnreadMessages(group.getId(), userId);

            // Tạo MessageListDTO
            String lastMessageContent = lastMessage != null ? lastMessage.getContent() : "Chưa có tin nhắn";
            String lastMessageTime = lastMessage != null ? lastMessage.getTimestamp() : null;

            return new MessageListDTO(
                    group.getId(),
                    group.getName(),
                    group.getAvatarUrl(),
                    lastMessageContent,
                    lastMessageTime,
                    unreadCount,
                    true // isGroup = true
            );
        }).collect(Collectors.toList());
    }

    @Override
    public void saveGroupMessage(GroupMessage message) {
        groupMessageRepository.save(message);
    }

    @Override
    public boolean isUserInGroup(Long userId, Long groupId) {
        return chatGroupRepository.existsByIdAndMembersId(groupId, userId);
    }

    @Override
    public List<Long> getGroupMembers(Long groupId) {
        return chatGroupRepository.findMemberIdsByGroupId(groupId);
    }

    @Override
    public List<MessageListDTO> getFriendLastMessages(Long userId) {
        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(userId);
        List<User> friends = new ArrayList<>();

        for (Friendship f : friendships) {
            User friend = f.getUser().getId().equals(userId) ? f.getFriend() : f.getUser();
            friends.add(friend);
        }

        return friends.stream().map(friend -> {
            // Lấy tin nhắn gần nhất giữa userId và friend
            ChatMessage lastMessage = chatMessageRepository.findTopByUsersOrderByTimestampDesc(userId, friend.getId());

            // Đếm số tin nhắn chưa đọc
            int unreadCount = chatMessageRepository.countUnreadMessages(userId, friend.getId());

            // Nếu không có tin nhắn, trả về "Chưa có tin nhắn"
            String lastMessageContent = lastMessage != null ? lastMessage.getContent() : "Chưa có tin nhắn";
            String lastMessageTime = lastMessage != null ? String.valueOf(lastMessage.getTimestamp()) : null;

            return new MessageListDTO(
                    friend.getId(),
                    friend.getUsername(),
                    friend.getAvatarUrl(),
                    lastMessageContent,
                    lastMessageTime,
                    unreadCount,
                    false // isGroup = false
            );
        }).collect(Collectors.toList());
    }

    @Override
    public void savePrivateMessage(ChatMessage message) {
        chatMessageRepository.save(message);
    }
}
