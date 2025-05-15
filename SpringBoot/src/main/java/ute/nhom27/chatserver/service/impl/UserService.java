package ute.nhom27.chatserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ute.nhom27.chatserver.dto.UserDTO;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.FriendshipRepository;
import ute.nhom27.chatserver.repository.UserRepository;
import ute.nhom27.chatserver.service.IUserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(keyword, keyword, keyword);
    }

    @Override
    public User saveUser(User user) {
        // Mã hóa mật khẩu trước khi lưu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public boolean existsByUsernameOrEmailOrPhone(String username, String email, String phone) {
        return userRepository.existsByUsername(username)
                || userRepository.existsByEmail(email)
                || userRepository.existsByPhone(phone);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setPublicKey(user.getPublicKey() != null ? user.getPublicKey() : "");
        userDTO.setNotificationToken(user.getNotificationToken() != null ? user.getNotificationToken() : "");
        userDTO.setAvatarURL(user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        userDTO.setThemePreference(user.getThemePreference() != null ? user.getThemePreference() : "light");
        return userDTO;
    }

    @Override
    public int getFriendsCount(Long userId) {
        return friendshipRepository.countByUserIdAndStatus(userId, "ACCEPTED");
    }

    @Override
    public User updateAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file duy nhất
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Lưu file
            Files.copy(file.getInputStream(), filePath);

            // Cập nhật URL avatar trong database
            String avatarUrl = "/uploads/" + fileName;
            user.setAvatarUrl(avatarUrl);
            return userRepository.save(user);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}