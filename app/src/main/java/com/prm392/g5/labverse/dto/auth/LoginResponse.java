package com.prm392.g5.labverse.dto.auth;

public class LoginResponse{
        private String accessToken;
        private String userId;

    public LoginResponse(String accessToken, String userId) {
        this.accessToken = accessToken;
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUserId() {
        return userId;
    }
}
