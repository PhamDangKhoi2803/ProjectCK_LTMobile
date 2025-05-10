package ute.nhom27.chatserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ute.nhom27.chatserver.entity.GroupMember;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    // Tìm tất cả thành viên theo ID nhóm
    List<GroupMember> findByChatGroupId(Long chatGroupId);

    // Kiểm tra thành viên đã tồn tại trong nhóm
    boolean existsByChatGroupIdAndUserId(Long chatGroupId, Long userId);

    // Xóa một thành viên khỏi nhóm
    void deleteByChatGroupIdAndUserId(Long chatGroupId, Long userId);

}
