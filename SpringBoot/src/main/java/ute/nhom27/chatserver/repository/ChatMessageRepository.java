package ute.nhom27.chatserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ute.nhom27.chatserver.entity.ChatMessage;

import java.util.List;

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
}