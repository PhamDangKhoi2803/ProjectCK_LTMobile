package ute.nhom27.chatserver.dto;

public class NotificationDTO {
    private Long senderId;
    private String usernameSender;
    private Long receiverId;
    private String type;
    private String content;

    public NotificationDTO(Long senderId, String usernameSender, Long receiverId, String type, String content) {
        this.senderId = senderId;
        this.usernameSender = usernameSender;
        this.receiverId = receiverId;
        this.type = type;
        this.content = content;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getUsernameSender() {
        return usernameSender;
    }

    public void setUsernameSender(String usernameSender) {
        this.usernameSender = usernameSender;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
