package com.prm392.g5.labverse.dto.auth;

public class LoginResponse{
        private String accessToken;
        private String userId;
        private String userRole;

    public LoginResponse(String accessToken, String userId, String userRole) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.userRole = userRole;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserRole() {
        return userRole;
    }
}
