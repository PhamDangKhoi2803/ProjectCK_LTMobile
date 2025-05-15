package ute.nhom27.chatserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ute.nhom27.chatserver.entity.GroupMember;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    // Tìm tất cả thành viên theo ID nhóm
    List<GroupMember> findByChatGroupId(Long chatGroupId);

    // Kiểm tra thành viên đã tồn tại trong nhóm
    boolean existsByChatGroupIdAndUserId(Long chatGroupId, Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM GroupMember gm WHERE gm.chatGroup.id = :groupId AND gm.user.id = :userId")
    int deleteByChatGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    int deleteAllByChatGroupId(Long chatGroupId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.user.id = :userId AND gm.chatGroup.id = :groupId")
    GroupMember findByUserIdAndChatGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);

}
