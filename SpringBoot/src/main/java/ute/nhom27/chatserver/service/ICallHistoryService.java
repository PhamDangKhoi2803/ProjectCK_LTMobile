package ute.nhom27.chatserver.service;

import java.util.List;
import ute.nhom27.chatserver.dto.CallHistoryDTO;
import ute.nhom27.chatserver.entity.CallHistory;

public interface ICallHistoryService {

    // Lưu thông tin cuộc gọi mới
    CallHistory saveCallHistory(CallHistory callHistory);

    // Thêm phương thức mới để lưu CallHistoryDTO
    CallHistoryDTO saveCallHistoryDTO(CallHistoryDTO callHistoryDTO);

    // Lấy lịch sử cuộc gọi của người dùng
    List<CallHistoryDTO> getUserCallHistory(Long userId);

    // Cập nhật trạng thái cuộc gọi
    CallHistoryDTO updateCallStatus(Long callId, CallHistoryDTO updatedCall);

    // Lấy chi tiết cuộc gọi
    CallHistoryDTO getCallDetails(Long callId);

    // Lấy cuộc gọi lỡ của người dùng
    List<CallHistoryDTO> getMissedCalls(Long userId);

    // Đếm tổng số cuộc gọi
    Long countUserCalls(Long userId);

    // Chuyển đổi Entity thành DTO
    CallHistoryDTO convertToDTO(CallHistory callHistory);
}