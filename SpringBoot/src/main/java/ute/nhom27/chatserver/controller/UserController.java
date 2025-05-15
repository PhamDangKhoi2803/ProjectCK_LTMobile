package ute.nhom27.chatserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ute.nhom27.chatserver.dto.PasswordChangeRequest;
import ute.nhom27.chatserver.dto.UserDTO;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.UserRepository;
import ute.nhom27.chatserver.service.IUserService;
import ute.nhom27.chatserver.util.JwtUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private IUserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam String keyword,
            @RequestParam Long currentUserId
    ) {
        // Kiểm tra từ khóa
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Từ khóa tìm kiếm không được để trống");
        }

        log.info("Searching users with keyword: {}, excluding currentUserId: {}", keyword, currentUserId);

        // Tìm kiếm người dùng
        List<User> users = userService.searchUsers(keyword.trim());

        // Chuyển đổi sang UserDTO và loại trừ người dùng hiện tại
        List<UserDTO> userDTOs = users.stream()
                .filter(user -> !user.getId().equals(currentUserId)) // Loại trừ người dùng hiện tại
                .map(userService::convertToDTO)
                .collect(Collectors.toList());

        if (userDTOs.isEmpty()) {
            return ResponseEntity.ok("Không tìm thấy người dùng nào phù hợp");
        }

        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().body("ID không được để trống");
        }

        log.info("Fetching user with id: {}", id);

        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(userService.convertToDTO(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/friends/count")
    public ResponseEntity<?> getFriendsCount(@PathVariable Long userId) {
        try {
            int count = userService.getFriendsCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting friends count for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Error getting friends count");
        }
    }

    @PutMapping("/{userId}/avatar")
    public ResponseEntity<?> updateAvatar(
            @PathVariable Long userId,
            @RequestBody String avatarUrl) {
        try {

            if (avatarUrl.startsWith("\"") && avatarUrl.endsWith("\"")) {
                avatarUrl = avatarUrl.substring(1, avatarUrl.length() - 1);
            }
            // Cập nhật avatar
            User updatedUser = userService.updateAvatar(userId, avatarUrl);
            log.info("Successfully updated avatar for user {}", userId);

            // Chuyển đổi sang DTO trước khi trả về
            UserDTO userDTO = userService.convertToDTO(updatedUser);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            log.error("Error updating avatar: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating avatar: " + e.getMessage());
        }
    }
    @PostMapping("/{userId}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long userId,
            @RequestBody PasswordChangeRequest request) {
        try {
            boolean success = userService.changePassword(
                    userId,
                    request.getCurrentPassword(),
                    request.getNewPassword()
            );

            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }
        } catch (Exception e) {
            log.error("Error changing password for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Error changing password");
        }
    }
}