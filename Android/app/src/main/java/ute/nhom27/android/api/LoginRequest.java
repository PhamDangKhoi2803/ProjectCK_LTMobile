package ute.nhom27.android.api;

public class LoginRequest {
    private String phoneOrEmail;
    private String password;
    // Getters, setters

    public LoginRequest(String username, String password) {
        this.phoneOrEmail = username;
        this.password = password;
    }

    public String getUsername() {
        return phoneOrEmail;
    }

    public void setUsername(String username) {
        this.phoneOrEmail = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
