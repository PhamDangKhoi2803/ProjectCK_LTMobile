package ute.nhom27.chatserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ute.nhom27.chatserver.dto.MessageListDTO;
import ute.nhom27.chatserver.dto.UserDTO;
import ute.nhom27.chatserver.entity.ChatMessage;
import ute.nhom27.chatserver.entity.Friendship;
import ute.nhom27.chatserver.entity.GroupMessage;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.ChatMessageRepository;
import ute.nhom27.chatserver.repository.FriendshipRepository;
import ute.nhom27.chatserver.repository.GroupMessageRepository;
import ute.nhom27.chatserver.service.IMessageService;

import java.time.LocalDateTime;
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

    @Override
    public List<ChatMessage> getPrivateMessages(Long user1Id, Long user2Id) {
        return chatMessageRepository.findAllMessagesBetweenUsers(user1Id, user2Id);
    }

    @Override
    public List<GroupMessage> getGroupMessages(Long groupId) {
        return groupMessageRepository.findByChatGroupIdOrderByTimestampAsc(groupId);
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

            // Nếu không có tin nhắn, trả về "Chưa có tin nhắn
            String lastMessageContent = lastMessage != null ? lastMessage.getContent() : "Chưa có tin nhắn";
            LocalDateTime lastMessageTimestamp = lastMessage != null ? lastMessage.getTimestamp() : null;
            boolean isSeen = lastMessage != null && "SEEN".equals(lastMessage.getStatus());

            return new MessageListDTO(
                    friend.getId(),
                    friend.getUsername(),
                    friend.getAvatarUrl(),
                    lastMessageContent,
                    lastMessageTimestamp,
                    isSeen
            );
        }).collect(Collectors.toList());
    }
}
