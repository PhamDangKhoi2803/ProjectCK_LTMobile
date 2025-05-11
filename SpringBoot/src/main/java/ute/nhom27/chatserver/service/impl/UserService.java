package ute.nhom27.chatserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.UserRepository;
import ute.nhom27.chatserver.service.IUserService;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
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
}