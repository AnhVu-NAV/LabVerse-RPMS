package com.prm392.g5.labverse.dto.auth;

public class VerifyForgotPasswordOtpResponse {
    private String resetPasswordToken;

    public VerifyForgotPasswordOtpResponse(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }
}
