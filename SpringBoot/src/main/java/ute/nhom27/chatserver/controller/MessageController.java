package ute.nhom27.chatserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ute.nhom27.chatserver.dto.MessageDTO;
import ute.nhom27.chatserver.dto.MessageListDTO;
import ute.nhom27.chatserver.entity.ChatMessage;
import ute.nhom27.chatserver.entity.GroupMessage;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.service.IFriendshipService;
import ute.nhom27.chatserver.service.IMessageService;
import ute.nhom27.chatserver.service.IUserService;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(FriendshipController.class);

    @Autowired
    private IMessageService messageService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IFriendshipService friendshipService;

    @Autowired
    private NotificationController notificationController;

    @GetMapping("/private")
    public List<ChatMessage> getPrivateMessages(
            @RequestParam Long userId1,
            @RequestParam Long userId2
    ) {
        return messageService.getPrivateMessages(userId1, userId2);
    }

    @GetMapping("/group/{groupId}")
    public List<GroupMessage> getGroupMessages(@PathVariable Long groupId) {
        return messageService.getGroupMessages(groupId);
    }

    @GetMapping("/friends/{userId}")
    public ResponseEntity<List<MessageListDTO>> getFriendLastMessages(@PathVariable Long userId) {
        List<MessageListDTO> messages = messageService.getFriendLastMessages(userId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendPrivateMessage(
            @RequestBody MessageDTO messageDTO
    ) {
        Long senderId = messageDTO.getSenderId();
        Long receiverId = messageDTO.getReceiverId();
        String content = messageDTO.getContent();
        String mediaUrl = messageDTO.getMediaUrl();
        String mediaType = messageDTO.getMediaType();

        // Kiểm tra người gửi và người nhận có tồn tại
        User sender = userService.getUserById(senderId).orElse(null);
        User receiver = userService.getUserById(receiverId).orElse(null);
        if (sender == null || receiver == null) {
            return ResponseEntity.badRequest().body("Người gửi hoặc người nhận không tồn tại");
        }

        // Kiểm tra xem có phải là chính mình
        if (senderId.equals(receiverId)) {
            return ResponseEntity.badRequest().body("Không thể gửi tin nhắn cho chính mình");
        }

        // Kiểm tra quan hệ bạn bè
        boolean areFriends = friendshipService.areFriends(senderId, receiverId);
        if (!areFriends) {
            return ResponseEntity.badRequest().body("Chỉ có thể gửi tin nhắn cho bạn bè");
        }

        // Kiểm tra dữ liệu đầu vào
        if ((content == null || content.trim().isEmpty()) && mediaUrl == null) {
            return ResponseEntity.badRequest().body("Tin nhắn phải có nội dung hoặc media");
        }

        // Kiểm tra mediaType nếu có mediaUrl
        if (mediaUrl != null && !mediaUrl.trim().isEmpty()) {
            if (mediaType == null || !(mediaType.equals("IMAGE") || mediaType.equals("VIDEO"))) {
                return ResponseEntity.badRequest().body("MediaType phải là 'IMAGE' hoặc 'VIDEO'");
            }
        }

        // Tạo và lưu tin nhắn
        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content != null ? content.trim() : "");
        message.setMediaUrl(mediaUrl);
        message.setMediaType(mediaType);
        message.setStatus("SENT");
        messageService.savePrivateMessage(message);

        // Gửi thông báo WebSocket
        log.info("Sending message notification to receiverId: {}, from senderId: {}", receiverId, senderId);
        String notificationContent = content != null && !content.trim().isEmpty() ? content : (mediaType != null ? "Đã gửi một " + mediaType.toLowerCase() : "Đã gửi một media");
        notificationController.sendFriendMessageNotification(receiverId, senderId, notificationContent);

        return ResponseEntity.ok("Tin nhắn đã được gửi");
    }

}