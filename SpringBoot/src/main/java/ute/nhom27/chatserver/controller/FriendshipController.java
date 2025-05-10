package ute.nhom27.chatserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ute.nhom27.chatserver.service.IFriendshipService;
import ute.nhom27.chatserver.service.IUserService;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    @Autowired
    private IFriendshipService friendshipService;

    @Autowired
    private IUserService userService;

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

        return ResponseEntity.ok("Đã gửi lời mời kết bạn");
    }

    // Chấp nhận lời mời kết bạn
    @PostMapping("/accept")
    public ResponseEntity<?> acceptFriendRequest(@RequestParam Long receiverId, @RequestParam Long senderId) {
        boolean success = friendshipService.acceptFriendRequest(senderId, receiverId);
        if (!success) {
            return ResponseEntity.badRequest().body("Không tìm thấy lời mời kết bạn hoặc đã xử lý rồi");
        }
        return ResponseEntity.ok("Đã chấp nhận lời mời kết bạn");
    }

    // Từ chối lời mời kết bạn
    @PostMapping("/reject")
    public ResponseEntity<?> rejectFriendRequest(@RequestParam Long receiverId, @RequestParam Long senderId) {
        boolean success = friendshipService.rejectFriendRequest(senderId, receiverId);
        if (!success) {
            return ResponseEntity.badRequest().body("Không tìm thấy lời mời kết bạn hoặc đã xử lý rồi");
        }
        return ResponseEntity.ok("Đã từ chối lời mời kết bạn");
    }
}