package com.prm392.g5.labverse.dto.auth;

public class LoginWGoogleRequest {
    String idToken;

    public LoginWGoogleRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
