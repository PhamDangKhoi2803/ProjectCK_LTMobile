package ute.nhom27.chatserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.SENT; // Mặc định là "ĐÃ GỬI"

    private LocalDateTime timestamp = LocalDateTime.now();

    private boolean isDeletedBySender = false;  // Xóa tin nhắn chỉ ở phía người gửi
    private boolean isDeletedByReceiver = false; // Xóa tin nhắn chỉ ở phía người nhận
    private boolean isRevoked = false; // Thu hồi tin nhắn (xóa cả 2 phía)

    public enum MessageStatus {
        SENT, // Đã gửi
        SEEN // Đã đọc
    }
}