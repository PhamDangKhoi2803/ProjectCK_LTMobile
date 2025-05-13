package ute.nhom27.chatserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MessageListDTO {
    private Long friendId;
    private String friendName;
    private String avatarUrl;
    private String lastMessage;
    private LocalDateTime timestamp;
    private Boolean isSeen; // SENT / SEEN

    public MessageListDTO(Long friendId, String friendName, String avatarUrl, String lastMessage, LocalDateTime timestamp, Boolean isSeen) {
        this.friendId = friendId;
        this.friendName = friendName;
        this.avatarUrl = avatarUrl;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getSeen() {
        return isSeen;
    }

    public void setSeen(Boolean seen) {
        isSeen = seen;
    }
}