package ute.nhom27.chatserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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
            "ORDER BY m.timestamp DESC " +
            "LIMIT 1")
    Optional<ChatMessage> findLatestMessageBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );

    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE ((m.sender.id = :friendId AND m.receiver.id = :userId) OR " +
            "(m.sender.id = :userId AND m.receiver.id = :friendId)) " +
            "AND m.status = 'SENT' " +
            "AND m.sender.id = :friendId " +
            "AND m.isRevoked = false")
    int countUnreadMessages(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage cm WHERE (cm.sender.id = :userId1 AND cm.receiver.id = :userId2) OR (cm.sender.id = :userId2 AND cm.receiver.id = :userId1)")
    void deleteMessagesBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}