package ute.nhom27.chatserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ute.nhom27.chatserver.entity.GroupMessage;

import java.util.List;

@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {

    List<GroupMessage> findByChatGroupIdOrderByTimestampAsc(Long groupId);

    @Query("SELECT gm FROM GroupMessage gm WHERE gm.chatGroup.id = :groupId " +
           "AND gm.isDeletedForUser = false AND gm.isRevoked = false " +
           "ORDER BY gm.timestamp DESC LIMIT 1")
    GroupMessage findLastMessageByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT COUNT(gm) FROM GroupMessage gm " +
           "WHERE gm.chatGroup.id = :groupId " +
           "AND gm.status = 'SENT' " +
           "AND gm.sender.id != :userId " +
           "AND gm.isDeletedForUser = false " +
           "AND gm.isRevoked = false")
    int countUnreadMessages(@Param("groupId") Long groupId, @Param("userId") Long userId);
    int deleteAllByChatGroupId(Long chatGroupId);
}