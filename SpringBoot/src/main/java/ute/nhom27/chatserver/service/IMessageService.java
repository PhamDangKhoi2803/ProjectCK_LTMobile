package ute.nhom27.chatserver.service;

import ute.nhom27.chatserver.dto.GroupMessageDTO;
import ute.nhom27.chatserver.dto.MessageDTO;
import ute.nhom27.chatserver.dto.MessageListDTO;
import ute.nhom27.chatserver.entity.ChatMessage;
import ute.nhom27.chatserver.entity.GroupMessage;

import java.util.List;

public interface IMessageService {
    List<MessageDTO> getPrivateMessages(Long user1Id, Long user2Id);

    List<GroupMessage> getGroupMessages(Long groupId);

    List<GroupMessageDTO> getGroupMessagesWithInfo(Long groupId);

    List<MessageListDTO> getGroupLastMessages(Long userId);

    void saveGroupMessage(GroupMessage message);

    boolean isUserInGroup(Long userId, Long groupId);

    List<Long> getGroupMembers(Long groupId);

    List<MessageListDTO> getFriendLastMessages(Long userId);

    void savePrivateMessage(ChatMessage message);

    boolean deleteMessagesBetweenUsers(Long userId1, Long userId2);
}
