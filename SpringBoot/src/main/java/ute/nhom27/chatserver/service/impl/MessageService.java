package ute.nhom27.chatserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ute.nhom27.chatserver.entity.ChatMessage;
import ute.nhom27.chatserver.entity.GroupMessage;
import ute.nhom27.chatserver.repository.ChatMessageRepository;
import ute.nhom27.chatserver.repository.GroupMessageRepository;
import ute.nhom27.chatserver.service.IMessageService;

import java.util.List;

@Service
public class MessageService implements IMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private GroupMessageRepository groupMessageRepository;

    @Override
    public List<ChatMessage> getPrivateMessages(Long user1Id, Long user2Id) {
        return chatMessageRepository.findAllMessagesBetweenUsers(user1Id, user2Id);
    }

    @Override
    public List<GroupMessage> getGroupMessages(Long groupId) {
        return groupMessageRepository.findByChatGroupIdOrderByTimestampAsc(groupId);
    }
}
