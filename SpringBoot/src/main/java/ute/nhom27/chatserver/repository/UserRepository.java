package ute.nhom27.chatserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ute.nhom27.chatserver.entity.User;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(String username, String email, String phone);
    Optional<User> findByPhoneOrEmail(String phone, String email);
}