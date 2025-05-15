package ute.nhom27.chatserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ute.nhom27.chatserver.dto.UserDTO;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.service.IUserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private IUserService userService;

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
}