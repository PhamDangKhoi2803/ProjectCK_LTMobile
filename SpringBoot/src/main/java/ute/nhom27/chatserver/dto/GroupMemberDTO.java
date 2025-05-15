package ute.nhom27.chatserver.dto;

public class GroupMemberDTO {
    private Long groupId;
    private Long userId;
    private String name;
    private String role;
    private String avatar;

    // Constructors
    public GroupMemberDTO() {}

    public GroupMemberDTO(Long groupId, Long userId, String name, String role, String avatar) {
        this.groupId = groupId;
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.avatar = avatar;
    }

    // Getters and Setters
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}