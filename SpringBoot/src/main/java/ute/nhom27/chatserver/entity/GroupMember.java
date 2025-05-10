package ute.nhom27.chatserver.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "group_members")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "chat_group_id", nullable = false)
    private ChatGroup chatGroup;

    @Column(nullable = false)
    private String role = "member"; // member hoặc admin

    @Column(nullable = false)
    private String joinedAt; // Định dạng chuỗi ISO 8601 (ví dụ: "2025-05-05T15:20:00Z")
}
