package ute.nhom27.chatserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ute.nhom27.chatserver.entity.CallHistory;

import java.util.List;

@Repository
public interface CallHistoryRepository extends JpaRepository<CallHistory, Long> {

    // Tìm cuộc gọi theo người gọi hoặc người nhận
    List<CallHistory> findByCallerIdOrReceiverIdOrderByStartTimeDesc(Long callerId, Long receiverId);

    // Đếm tổng số cuộc gọi của một người dùng
    @Query("SELECT COUNT(c) FROM CallHistory c WHERE c.callerId = :userId OR c.receiverId = :userId")
    Long countCallsByUserId(@Param("userId") Long userId);

    // Lấy cuộc gọi gần đây nhất
    @Query("SELECT c FROM CallHistory c WHERE c.callerId = :userId OR c.receiverId = :userId ORDER BY c.startTime DESC")
    List<CallHistory> findRecentCallsByUserId(@Param("userId") Long userId);

    // Tìm các cuộc gọi lỡ
    List<CallHistory> findByReceiverIdAndStatusOrderByStartTimeDesc(Long receiverId, String status);
}