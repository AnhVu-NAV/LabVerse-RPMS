package com.prm392.g5.labverse.dto.auth;

public class LoginResponse{
        String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public LoginResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
