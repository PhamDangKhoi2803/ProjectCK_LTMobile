package ute.nhom27.chatserver.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long senderId;
    private Long receiverId;
    private String content; // Nội dung tin nhắn văn bản
    private String mediaUrl; // URL của video/ảnh (nếu có)
    private String mediaType; // Loại media: "IMAGE", "VIDEO" hoặc null (nếu chỉ có văn bản)
    private LocalDateTime timestamp;
    private String status;
    private boolean isGroup;

    public MessageDTO(Long senderId, Long receiverId, String content, String mediaUrl, String mediaType, LocalDateTime timestamp, String status, boolean isGroup) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.timestamp = timestamp;
        this.status = status;
        this.isGroup = isGroup;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }
}
