package ute.nhom27.chatserver.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(nullable = false)
    private String password;

    private String publicKey;

    private String notificationToken;

    private String themePreference;

    @Transient // không lưu trong database (vì trạng thái online là tạm thời)
    private boolean isOnline;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Friendship> friendships;

}