package ute.nhom27.chatserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ute.nhom27.chatserver.dto.CallHistoryDTO;
import ute.nhom27.chatserver.entity.CallHistory;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.UserRepository;
import ute.nhom27.chatserver.service.ICallHistoryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calls")
public class CallHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(CallHistoryController.class);

    @Autowired
    private ICallHistoryService callHistoryService;

    @Autowired
    private UserRepository userRepository;

    // Lưu lịch sử cuộc gọi mới từ DTO
    @PostMapping
    public ResponseEntity<?> saveCallHistory(@RequestBody CallHistoryDTO callHistoryDTO) {
        try {
            logger.info("Saving call history: {} to {}, type: {}",
                    callHistoryDTO.getCallerId(), callHistoryDTO.getReceiverId(), callHistoryDTO.getCallType());

            // Kiểm tra người gọi và người nhận
            User caller = userRepository.findById(callHistoryDTO.getCallerId()).orElse(null);
            User receiver = userRepository.findById(callHistoryDTO.getReceiverId()).orElse(null);

            if (caller == null || receiver == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Người dùng không tồn tại"
                ));
            }

            // Bổ sung thông tin tên và avatar nếu chưa có
            if (callHistoryDTO.getCallerName() == null) {
                callHistoryDTO.setCallerName(caller.getUsername());
            }

            if (callHistoryDTO.getCallerAvatar() == null) {
                callHistoryDTO.setCallerAvatar(caller.getAvatarUrl());
            }

            if (callHistoryDTO.getReceiverName() == null) {
                callHistoryDTO.setReceiverName(receiver.getUsername());
            }

            if (callHistoryDTO.getReceiverAvatar() == null) {
                callHistoryDTO.setReceiverAvatar(receiver.getAvatarUrl());
            }

            // Lưu lịch sử cuộc gọi
            CallHistoryDTO savedCall = callHistoryService.saveCallHistoryDTO(callHistoryDTO);

            return ResponseEntity.ok(Map.of(
                    "message", "Đã lưu lịch sử cuộc gọi",
                    "callId", savedCall.getId()
            ));

        } catch (Exception e) {
            logger.error("Error saving call history: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lưu lịch sử cuộc gọi: " + e.getMessage()));
        }
    }

    // Lấy lịch sử cuộc gọi của một người dùng
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CallHistoryDTO>> getUserCallHistory(@PathVariable Long userId) {
        try {
            List<CallHistoryDTO> calls = callHistoryService.getUserCallHistory(userId);
            return ResponseEntity.ok(calls);
        } catch (Exception e) {
            logger.error("Error getting call history for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Cập nhật trạng thái cuộc gọi
    @PutMapping("/{callId}")
    public ResponseEntity<?> updateCallStatus(
            @PathVariable Long callId,
            @RequestBody CallHistoryDTO callHistoryDTO) {
        try {
            logger.info("Updating call {}: status={}, endTime={}, duration={}",
                    callId, callHistoryDTO.getStatus(), callHistoryDTO.getEndTime(), callHistoryDTO.getDuration());

            CallHistoryDTO updatedCall = callHistoryService.updateCallStatus(callId, callHistoryDTO);

            if (updatedCall == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Đã cập nhật trạng thái cuộc gọi",
                    "call", updatedCall
            ));

        } catch (Exception e) {
            logger.error("Error updating call {}: {}", callId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi cập nhật trạng thái cuộc gọi: " + e.getMessage()));
        }
    }

    // Lấy thông tin một cuộc gọi
    @GetMapping("/{callId}")
    public ResponseEntity<CallHistoryDTO> getCallDetails(@PathVariable Long callId) {
        try {
            CallHistoryDTO call = callHistoryService.getCallDetails(callId);

            if (call == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(call);

        } catch (Exception e) {
            logger.error("Error getting call details {}: {}", callId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy cuộc gọi lỡ
    @GetMapping("/missed/{userId}")
    public ResponseEntity<List<CallHistoryDTO>> getMissedCalls(@PathVariable Long userId) {
        try {
            List<CallHistoryDTO> missedCalls = callHistoryService.getMissedCalls(userId);
            return ResponseEntity.ok(missedCalls);
        } catch (Exception e) {
            logger.error("Error getting missed calls for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}