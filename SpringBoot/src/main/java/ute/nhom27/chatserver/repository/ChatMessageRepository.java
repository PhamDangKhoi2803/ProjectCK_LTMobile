package ute.nhom27.chatserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ute.nhom27.chatserver.entity.ChatMessage;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "((m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
            " (m.sender.id = :user2Id AND m.receiver.id = :user1Id)) " +
            "AND m.isRevoked = false " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> findAllMessagesBetweenUsers(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id
    );

    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
            "(m.sender.id = :userId2 AND m.receiver.id = :userId1) " +
            "ORDER BY m.timestamp DESC")
    ChatMessage findTopByUsersOrderByTimestampDesc(Long userId1, Long userId2);
}