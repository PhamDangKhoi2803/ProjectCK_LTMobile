package ute.nhom27.chatserver.service;

import org.springframework.web.multipart.MultipartFile;
import ute.nhom27.chatserver.dto.UserDTO;
import ute.nhom27.chatserver.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByPhone(String phone);
    List<User> searchUsers(String keyword);
    User saveUser(User user);
    boolean existsByUsernameOrEmailOrPhone(String username, String email, String phone);
    void deleteUser(Long id);

    UserDTO convertToDTO(User user);

    // Thêm các phương thức mới
    int getFriendsCount(Long userId);
    User updateAvatar(Long userId, MultipartFile file);
    boolean changePassword(Long userId, String currentPassword, String newPassword);
}
