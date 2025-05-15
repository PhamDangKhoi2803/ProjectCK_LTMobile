package ute.nhom27.chatserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ute.nhom27.chatserver.dto.UserDTO;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.service.IFriendshipService;
import ute.nhom27.chatserver.service.IUserService;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    private static final Logger log = LoggerFactory.getLogger(FriendshipController.class);
    @Autowired
    private IFriendshipService friendshipService;

    @Autowired
    private IUserService userService;

    @Autowired
    private NotificationController notificationController;

    // Gửi lời mời kết bạn
    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(@RequestParam Long senderId, @RequestParam Long receiverId) {
        if (senderId.equals(receiverId)) {
            return ResponseEntity.badRequest().body("Không thể kết bạn với chính mình");
        }

        boolean senderExists = userService.getUserById(senderId).isPresent();
        boolean receiverExists = userService.getUserById(receiverId).isPresent();

        if (!senderExists || !receiverExists) {
            return ResponseEntity.badRequest().body("Người dùng không tồn tại");
        }

        boolean success = friendshipService.sendFriendRequest(senderId, receiverId);
        if (!success) {
            return ResponseEntity.badRequest().body("Đã tồn tại lời mời hoặc là bạn bè");
        }

        // Gửi thông báo qua NotificationController
        log.info("Sending friend request notification to receiverId: {}, from senderId: {}", receiverId, senderId);
        String content = "Bạn nhận được lời mời kết bạn từ " + userService.getUserById(senderId)
                .map(User::getUsername).orElse("Unknown");
        notificationController.sendNotification(receiverId, senderId, content, "FRIEND_REQUEST");

        return ResponseEntity.ok("Đã gửi lời mời kết bạn");
    }

    // Chấp nhận lời mời kết bạn
    @PostMapping("/accept")
    public ResponseEntity<?> acceptFriendRequest(@RequestParam Long receiverId, @RequestParam Long senderId) {
        boolean success = friendshipService.acceptFriendRequest(senderId, receiverId);
        if (!success) {
            return ResponseEntity.badRequest().body("Không tìm thấy lời mời kết bạn hoặc đã xử lý rồi");
        }

        // Gửi thông báo chấp nhận
        String content = userService.getUserById(senderId)
                .map(User::getUsername).orElse("Unknown") + " đã chấp nhận lời mời kết bạn của bạn";
        notificationController.sendNotification(senderId, receiverId, content, "FRIEND_ACCEPT");

        return ResponseEntity.ok("Đã chấp nhận lời mời kết bạn");
    }

    // Từ chối lời mời kết bạn
    @PostMapping("/reject")
    public ResponseEntity<?> rejectFriendRequest(@RequestParam Long receiverId, @RequestParam Long senderId) {
        boolean success = friendshipService.rejectFriendRequest(senderId, receiverId);
        if (!success) {
            return ResponseEntity.badRequest().body("Không tìm thấy lời mời kết bạn hoặc đã xử lý rồi");
        }

        // Gửi thông báo từ chối
        String content = userService.getUserById(senderId)
                .map(User::getUsername).orElse("Unknown") + " đã từ chối lời mời kết bạn của bạn";
        notificationController.sendNotification(senderId, receiverId, content, "FRIEND_REJECT");

        return ResponseEntity.ok("Đã từ chối lời mời kết bạn");
    }

    @GetMapping("/{userId}/list")
    public ResponseEntity<?> getFriends(@PathVariable Long userId) {
        List<UserDTO> friends = friendshipService.getFriends(userId);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/{userId}/list-request")
    public ResponseEntity<?> getFriendRequests(@PathVariable Long userId) {
        List<UserDTO> requests = friendshipService.getFriendRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{userId}/sent-requests")
    public ResponseEntity<?> getSentFriendRequests(@PathVariable Long userId) {
        List<UserDTO> sentRequests = friendshipService.getSentFriendRequests(userId);
        return ResponseEntity.ok(sentRequests);
    }

    @GetMapping("/{userId}/non-friends")
    public ResponseEntity<?> getNonFriendUsers(@PathVariable Long userId) {
        List<UserDTO> nonFriends = friendshipService.getNonFriendUsers(userId);
        return ResponseEntity.ok(nonFriends);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFriendRequest(@RequestParam Long senderId, @RequestParam Long receiverId) {
        boolean success = friendshipService.rejectFriendRequest(senderId, receiverId);
        if (!success) {
            return ResponseEntity.badRequest().body("Không tìm thấy lời mời kết bạn hoặc đã xử lý rồi");
        }

        // Gửi thông báo thu hồi
        String content = userService.getUserById(senderId)
                .map(User::getUsername).orElse("Unknown") + " đã thu hồi lời mời kết bạn";
        notificationController.sendNotification(receiverId, senderId, content, "FRIEND_REQUEST_WITHDRAWN");

        return ResponseEntity.ok("Đã thu hồi lời mời kết bạn");
    }

    @PostMapping("/unfriend")
    public ResponseEntity<?> unfriend(@RequestParam Long userId, @RequestParam Long friendId) {
        if (userId.equals(friendId)) {
            return ResponseEntity.badRequest().body("Không thể thực hiện với chính mình");
        }

        boolean userExists = userService.getUserById(userId).isPresent();
        boolean friendExists = userService.getUserById(friendId).isPresent();

        if (!userExists || !friendExists) {
            return ResponseEntity.badRequest().body("Người dùng không tồn tại");
        }

        boolean success = friendshipService.unfriend(userId, friendId);
        if (!success) {
            return ResponseEntity.badRequest().body("Không phải là bạn bè hoặc đã xử lý rồi");
        }

        // Gửi thông báo hủy kết bạn
        String content = userService.getUserById(userId)
                .map(User::getUsername).orElse("Unknown") + " đã hủy kết bạn với bạn";
        notificationController.sendNotification(friendId, userId, content, "UNFRIEND");

        return ResponseEntity.ok("Đã hủy kết bạn thành công");
    }
}