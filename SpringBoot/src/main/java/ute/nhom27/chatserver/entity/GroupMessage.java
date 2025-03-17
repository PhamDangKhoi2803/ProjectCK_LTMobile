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

    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.SENT;

    private LocalDateTime timestamp = LocalDateTime.now();

    private boolean isDeletedForUser = false; // Xóa tin nhắn chỉ ở phía người dùng
    private boolean isRevoked = false; // Thu hồi tin nhắn (xóa cả nhóm)

    public enum MessageStatus {
        SENT, // Đã gửi
        SEEN // Đã đọc
    }
}
