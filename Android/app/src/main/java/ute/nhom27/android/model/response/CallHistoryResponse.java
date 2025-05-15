package ute.nhom27.android.model.response;

public class CallHistoryResponse {
    private Long id;
    private Long callerId;
    private Long receiverId;
    private String callType; // "VIDEO" hoặc "AUDIO"
    private String status; // "MISSED", "ANSWERED", "DECLINED"
    private String startTime;
    private String endTime;
    private int duration;

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

    public CallHistoryResponse(Long id, Long callerId, Long receiverId, String callType, String status, String startTime, String endTime, int duration) {
        this.id = id;
        this.callerId = callerId;
        this.receiverId = receiverId;
        this.callType = callType;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }
}
