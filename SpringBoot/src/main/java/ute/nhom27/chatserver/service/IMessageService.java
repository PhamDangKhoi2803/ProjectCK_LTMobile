package ute.nhom27.chatserver.service;

import ute.nhom27.chatserver.dto.MessageListDTO;
import ute.nhom27.chatserver.entity.ChatMessage;
import ute.nhom27.chatserver.entity.GroupMessage;

import java.util.List;

public interface IMessageService {
    List<ChatMessage> getPrivateMessages(Long user1Id, Long user2Id);

    List<GroupMessage> getGroupMessages(Long groupId);

    List<MessageListDTO> getFriendLastMessages(Long userId);
}
