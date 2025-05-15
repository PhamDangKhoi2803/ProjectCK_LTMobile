package ute.nhom27.chatserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ute.nhom27.chatserver.dto.CallHistoryDTO;
import ute.nhom27.chatserver.entity.CallHistory;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.CallHistoryRepository;
import ute.nhom27.chatserver.repository.UserRepository;
import ute.nhom27.chatserver.service.ICallHistoryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CallHistoryServiceImpl implements ICallHistoryService {

    @Autowired
    private CallHistoryRepository callHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    // Phương thức gốc lưu CallHistory entity
    @Override
    public CallHistory saveCallHistory(CallHistory callHistory) {
        return callHistoryRepository.save(callHistory);
    }

    // Thêm phương thức mới để lưu CallHistoryDTO
    @Override
    public CallHistoryDTO saveCallHistoryDTO(CallHistoryDTO callHistoryDTO) {
        // Chuyển từ DTO sang entity
        CallHistory callHistory = new CallHistory();
        callHistory.setCallerId(callHistoryDTO.getCallerId());
        callHistory.setReceiverId(callHistoryDTO.getReceiverId());
        callHistory.setCallType(callHistoryDTO.getCallType());
        callHistory.setStatus(callHistoryDTO.getStatus());
        callHistory.setStartTime(callHistoryDTO.getStartTime());
        callHistory.setEndTime(callHistoryDTO.getEndTime());
        callHistory.setDuration(callHistoryDTO.getDuration());

        // Lưu entity
        CallHistory savedCall = callHistoryRepository.save(callHistory);

        // Chuyển lại thành DTO và trả về
        return convertToDTO(savedCall);
    }

    @Override
    public List<CallHistoryDTO> getUserCallHistory(Long userId) {
        List<CallHistory> calls = callHistoryRepository.findByCallerIdOrReceiverIdOrderByStartTimeDesc(userId, userId);
        List<CallHistoryDTO> dtoList = new ArrayList<>();

        for (CallHistory call : calls) {
            dtoList.add(convertToDTO(call));
        }

        return dtoList;
    }

    @Override
    public CallHistoryDTO updateCallStatus(Long callId, CallHistoryDTO callHistoryDTO) {
        Optional<CallHistory> callOpt = callHistoryRepository.findById(callId);

        if (!callOpt.isPresent()) {
            return null;
        }

        CallHistory existingCall = callOpt.get();

        // Cập nhật các trường từ DTO
        if (callHistoryDTO.getStatus() != null) {
            existingCall.setStatus(callHistoryDTO.getStatus());
        }

        if (callHistoryDTO.getEndTime() != null) {
            existingCall.setEndTime(callHistoryDTO.getEndTime());
        }

        if (callHistoryDTO.getDuration() > 0) {
            existingCall.setDuration(callHistoryDTO.getDuration());
        }

        // Lưu và chuyển đổi lại thành DTO
        CallHistory updatedCall = callHistoryRepository.save(existingCall);
        return convertToDTO(updatedCall);
    }

    @Override
    public CallHistoryDTO getCallDetails(Long callId) {
        Optional<CallHistory> callOpt = callHistoryRepository.findById(callId);
        return callOpt.map(this::convertToDTO).orElse(null);
    }

    @Override
    public List<CallHistoryDTO> getMissedCalls(Long userId) {
        List<CallHistory> missedCalls = callHistoryRepository.findByReceiverIdAndStatusOrderByStartTimeDesc(userId, "MISSED");
        List<CallHistoryDTO> dtoList = new ArrayList<>();

        for (CallHistory call : missedCalls) {
            dtoList.add(convertToDTO(call));
        }

        return dtoList;
    }

    @Override
    public Long countUserCalls(Long userId) {
        return callHistoryRepository.countCallsByUserId(userId);
    }

    @Override
    public CallHistoryDTO convertToDTO(CallHistory callHistory) {
        CallHistoryDTO dto = new CallHistoryDTO();
        dto.setId(callHistory.getId());
        dto.setCallerId(callHistory.getCallerId());
        dto.setReceiverId(callHistory.getReceiverId());
        dto.setCallType(callHistory.getCallType());
        dto.setStatus(callHistory.getStatus());
        dto.setStartTime(callHistory.getStartTime());
        dto.setEndTime(callHistory.getEndTime());
        dto.setDuration(callHistory.getDuration());

        // Lấy thông tin người gọi
        User caller = userRepository.findById(callHistory.getCallerId()).orElse(null);
        if (caller != null) {
            dto.setCallerName(caller.getUsername());
            dto.setCallerAvatar(caller.getAvatarUrl());
        }

        // Lấy thông tin người nhận
        User receiver = userRepository.findById(callHistory.getReceiverId()).orElse(null);
        if (receiver != null) {
            dto.setReceiverName(receiver.getUsername());
            dto.setReceiverAvatar(receiver.getAvatarUrl());
        }

        return dto;
    }
}