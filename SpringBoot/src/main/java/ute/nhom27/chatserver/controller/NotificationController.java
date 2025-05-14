package ute.nhom27.chatserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import ute.nhom27.chatserver.dto.NotificationDTO;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.service.IUserService;

@Component
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private IUserService userService;

    // Gửi thông báo chung
    public void sendNotification(Long receiverId, Long senderId, String content, String type) {
        User sender = userService.getUserById(senderId).orElse(null);
        String usernameSender = sender != null ? sender.getUsername() : "Unknown";

        NotificationDTO notification = new NotificationDTO(
                senderId,
                usernameSender,
                receiverId,
                type != null ? type : "FRIEND_REQUEST",
                content
        );

        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/topic/notifications",
                notification
        );
        log.info("Sent notification to user {}: senderId={}, usernameSender={}, type={}, content={}",
                receiverId, senderId, usernameSender, type, content);
    }

    // Gửi thông báo tin nhắn
    public void sendFriendMessageNotification(Long receiverId, Long senderId, String messageContent) {
        User sender = userService.getUserById(senderId).orElse(null);
        String usernameSender = sender != null ? sender.getUsername() : "Unknown";
        String content = usernameSender + ": " + messageContent;

        NotificationDTO notification = new NotificationDTO(
                senderId,
                usernameSender,
                receiverId,
                "FRIEND_MESSAGE",
                content
        );

        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/topic/notifications",
                notification
        );
        log.info("Sent friend message notification to user {}: senderId={}, usernameSender={}, content={}",
                receiverId, senderId, usernameSender, messageContent);
    }
}