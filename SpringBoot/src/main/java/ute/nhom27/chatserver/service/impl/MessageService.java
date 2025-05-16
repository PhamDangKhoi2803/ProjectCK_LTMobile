package ute.nhom27.chatserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ute.nhom27.chatserver.dto.GroupMessageDTO;
import ute.nhom27.chatserver.dto.MessageDTO;
import ute.nhom27.chatserver.dto.MessageListDTO;
import ute.nhom27.chatserver.entity.ChatGroup;
import ute.nhom27.chatserver.entity.ChatMessage;
import ute.nhom27.chatserver.entity.Friendship;
import ute.nhom27.chatserver.entity.GroupMessage;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.*;
import ute.nhom27.chatserver.service.IMessageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Override
    public List<MessageDTO> getPrivateMessages(Long user1Id, Long user2Id) {
        List<ChatMessage> messages = chatMessageRepository.findAllMessagesBetweenUsers(user1Id, user2Id);

        return messages.stream().map(message -> new MessageDTO(
                        message.getSender().getId(),
                        message.getReceiver().getId(),
                        message.getContent(),
                        message.getMediaUrl(),
                        message.getMediaType(),
                        message.getTimestamp(),
                        message.getStatus(),
                        false  // isGroup = false vì đây là tin nhắn cá nhân
                )).collect(Collectors.toList());
    }

    @Override
    public List<GroupMessage> getGroupMessages(Long groupId) {
        return groupMessageRepository.findByChatGroupIdOrderByTimestampAsc(groupId);
    }

    @Override
    public List<GroupMessageDTO> getGroupMessagesWithInfo(Long groupId) {
        List<GroupMessage> messages = groupMessageRepository.findByChatGroupIdOrderByTimestampAsc(groupId);
        List<GroupMessageDTO> dtos = new ArrayList<>();

        for (GroupMessage message : messages) {
            User sender = message.getSender();
            GroupMessageDTO dto = new GroupMessageDTO(
                    groupId,
                    sender.getId(),
                    sender.getUsername(),
                    sender.getAvatarUrl(),
                    message.getStatus(),
                    message.getContent(),
                    message.getMediaUrl(),
                    message.getMediaType(),
                    message.getTimestamp().toString()
            );
            dtos.add(dto);
        }

        return dtos;
    }

    @Override
    public List<MessageListDTO> getGroupLastMessages(Long userId) {
        List<ChatGroup> userGroups = chatGroupRepository.findGroupsByUserId(userId);

        // Lấy danh sách MessageListDTO cho tất cả nhóm
        List<MessageListDTO> messageList = userGroups.stream().map(group -> {
            GroupMessage lastMessage = groupMessageRepository.findLastMessageByGroupId(group.getId());
            int unreadCount = groupMessageRepository.countUnreadMessages(group.getId(), userId);

            String lastMessageContent = lastMessage != null ? lastMessage.getContent() : "Chưa có tin nhắn";
            String lastMessageTime = lastMessage != null ? String.valueOf(lastMessage.getTimestamp()) : null;

            return new MessageListDTO(
                    group.getId(),
                    group.getName(),
                    group.getAvatarUrl(),
                    lastMessageContent,
                    lastMessageTime,
                    unreadCount,
                    true
            );
        }).collect(Collectors.toList());

        // Sắp xếp theo thời gian tin nhắn mới nhất
        messageList.sort((a, b) -> {
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });

        return messageList;
    }

    @Override
    public void saveGroupMessage(GroupMessage message) {
        groupMessageRepository.save(message);
    }

    @Override
    public boolean isUserInGroup(Long userId, Long groupId) {
        return groupMemberRepository.existsByChatGroupIdAndUserId(groupId, userId);
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

        // Lấy danh sách MessageListDTO cho tất cả bạn bè
        List<MessageListDTO> messageList = friends.stream().map(friend -> {
            Optional<ChatMessage> lastMessageOpt = chatMessageRepository.findLatestMessageBetweenUsers(userId, friend.getId());
            int unreadCount = chatMessageRepository.countUnreadMessages(userId, friend.getId());

            String lastMessageContent;
            String lastMessageTime;

            if (lastMessageOpt.isPresent()) {
                ChatMessage lastMessage = lastMessageOpt.get();

                if (lastMessage.getMediaUrl() != null && !lastMessage.getMediaUrl().isEmpty()) {
                    if ("IMAGE".equals(lastMessage.getMediaType())) {
                        lastMessageContent = "Đã gửi một hình ảnh";
                    } else if ("VIDEO".equals(lastMessage.getMediaType())) {
                        lastMessageContent = "Đã gửi một video";
                    } else {
                        lastMessageContent = lastMessage.getContent();
                    }
                } else {
                    lastMessageContent = lastMessage.getContent();
                }

                lastMessageTime = String.valueOf(lastMessage.getTimestamp());
            } else {
                lastMessageContent = "Chưa có tin nhắn";
                lastMessageTime = null;
            }

            return new MessageListDTO(
                    friend.getId(),
                    friend.getUsername(),
                    friend.getAvatarUrl(),
                    lastMessageContent,
                    lastMessageTime,
                    unreadCount,
                    false
            );
        }).collect(Collectors.toList());

        // Sắp xếp theo thời gian tin nhắn mới nhất
        messageList.sort((a, b) -> {
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });

        return messageList;
    }

    @Override
    public void savePrivateMessage(ChatMessage message) {
        chatMessageRepository.save(message);
    }

    @Override
    public boolean deleteMessagesBetweenUsers(Long userId1, Long userId2) {
        try {
            // Xóa tất cả tin nhắn giữa hai người dùng
            chatMessageRepository.deleteMessagesBetweenUsers(userId1, userId2);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
