package ute.nhom27.chatserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "group_messages")
public class GroupMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "chat_group_id", nullable = false)
    private ChatGroup chatGroup;

    @Column(nullable = false)
    private String content;

    private String mediaUrl;

    private String mediaType; // ví dụ: "image", "video", v.v.

    @Column(nullable = false)
    private String status = "SENT"; // Có thể là "SENT", "SEEN", v.v.

    @Column(nullable = false)
    private String timestamp; // ISO 8601 string như trên Android

    @Column(nullable = false)
    private boolean isDeletedForUser = false;

    @Column(nullable = false)
    private boolean isRevoked = false;

}
