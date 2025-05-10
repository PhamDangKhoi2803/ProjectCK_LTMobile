package ute.nhom27.android.api;

public class AISuggestionRequest {
    private Long userId;
    private String messageContent;
    // Getters, setters

    public AISuggestionRequest(Long userId, String messageContent) {
        this.userId = userId;
        this.messageContent = messageContent;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
}
