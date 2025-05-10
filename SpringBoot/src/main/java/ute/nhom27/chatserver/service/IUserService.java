package ute.nhom27.chatserver.service;

import ute.nhom27.chatserver.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    List<User> searchUsers(String keyword);
    User saveUser(User user);
    boolean existsByUsernameOrEmailOrPhone(String username, String email, String phone);
}
