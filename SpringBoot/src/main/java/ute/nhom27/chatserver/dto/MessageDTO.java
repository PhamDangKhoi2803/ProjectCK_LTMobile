package ute.nhom27.chatserver.dto;

import lombok.Data;

@Data
public class MessageDTO {
    private Long senderId;
    private Long receiverId;
    private String content; // Nội dung tin nhắn văn bản
    private String mediaUrl; // URL của video/ảnh (nếu có)
    private String mediaType; // Loại media: "IMAGE", "VIDEO" hoặc null (nếu chỉ có văn bản)

    public MessageDTO(Long senderId, Long receiverId, String content, String mediaUrl, String mediaType) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
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
}
