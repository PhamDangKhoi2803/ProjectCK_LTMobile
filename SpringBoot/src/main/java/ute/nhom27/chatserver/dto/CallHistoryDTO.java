package ute.nhom27.chatserver.dto;

public class CallHistoryDTO {
    private Long id;
    private Long callerId;
    private Long receiverId;
    private String callerName;
    private String receiverName;
    private String callerAvatar;
    private String receiverAvatar;
    private String callType;
    private String status;
    private String startTime;
    private String endTime;
    private int duration;

    public CallHistoryDTO() {
    }

    public CallHistoryDTO(Long id, Long callerId, Long receiverId, String callerName,
                          String receiverName, String callerAvatar, String receiverAvatar,
                          String callType, String status, String startTime, String endTime, int duration) {
        this.id = id;
        this.callerId = callerId;
        this.receiverId = receiverId;
        this.callerName = callerName;
        this.receiverName = receiverName;
        this.callerAvatar = callerAvatar;
        this.receiverAvatar = receiverAvatar;
        this.callType = callType;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCallerId() {
        return callerId;
    }

    public void setCallerId(Long callerId) {
        this.callerId = callerId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getCallerAvatar() {
        return callerAvatar;
    }

    public void setCallerAvatar(String callerAvatar) {
        this.callerAvatar = callerAvatar;
    }

    public String getReceiverAvatar() {
        return receiverAvatar;
    }

    public void setReceiverAvatar(String receiverAvatar) {
        this.receiverAvatar = receiverAvatar;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}