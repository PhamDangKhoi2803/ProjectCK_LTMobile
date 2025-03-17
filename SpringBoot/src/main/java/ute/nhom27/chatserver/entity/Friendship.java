package ute.nhom27.chatserver.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "friendships")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    private String nickname; // Biệt danh do người dùng đặt cho bạn bè

    public enum FriendshipStatus {
        PENDING, ACCEPTED, BLOCKED
    }
}
