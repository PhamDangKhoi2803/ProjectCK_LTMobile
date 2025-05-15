package ute.nhom27.android.model.response;

public class GroupMessageResponse {
    private Long groupId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String status;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private String timestamp;

    // Constructor mặc định
    public GroupMessageResponse() {}

    // Constructor đầy đủ tham số
    public GroupMessageResponse(Long groupId, Long senderId, String senderName,
                                String senderAvatar, String status, String content,
                                String mediaUrl, String mediaType, String timestamp) {
        this.groupId = groupId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.status = status;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.timestamp = timestamp;
    }

    // Getters và Setters
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}