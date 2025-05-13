package ute.nhom27.android.model.response;

public class MessageListResponse {

    private Long friendId;
    private String friendName;
    private String avatarUrl;
    private String lastMessage;
    private String timestamp; // ISO 8601 dạng String
    private Boolean isSeen;

    // Getters và setters
    public Long getFriendId() { return friendId; }
    public void setFriendId(Long friendId) { this.friendId = friendId; }

    public String getFriendName() { return friendName; }
    public void setFriendName(String friendName) { this.friendName = friendName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Boolean getIsSeen() { return isSeen; }
    public void setIsSeen(Boolean isSeen) { this.isSeen = isSeen; }
}
