package ute.nhom27.android.model;

import com.google.gson.annotations.SerializedName;

public class GroupMember {
    @SerializedName("id")
    private Long id;

    @SerializedName("groupId")
    private Long groupId;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("joinedAt")
    private String joinedAt;

    @SerializedName("user")
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
