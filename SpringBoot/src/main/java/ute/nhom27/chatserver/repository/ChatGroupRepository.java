package ute.nhom27.chatserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ute.nhom27.chatserver.entity.ChatGroup;

import java.util.List;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    // Lấy tất cả nhóm của user (bao gồm cả nhóm user là admin và member)
    @Query("SELECT DISTINCT cg FROM ChatGroup cg " +
            "JOIN cg.members m " +
            "WHERE m.user.id = :userId")
    List<ChatGroup> findGroupsByUserId(@Param("userId") Long userId);


    boolean existsByIdAndMembersId(Long groupId, Long userId);

    @Query("SELECT m.id FROM ChatGroup cg JOIN cg.members m WHERE cg.id = :groupId")
    List<Long> findMemberIdsByGroupId(@Param("groupId") Long groupId);


}
