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

    private String mediaUrl;
    private String mediaType;

    @Column(nullable = false)
    private String status = "SENT"; // SENT, DELIVERED, SEEN

    private LocalDateTime timestamp = LocalDateTime.now();

    private boolean isDeletedBySender = false;
    private boolean isDeletedByReceiver = false;
    private boolean isRevoked = false;
}
