package com.prm392.g5.labverse.repository;

import com.prm392.g5.labverse.apiService.AuthApiService;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dto.auth.LoginRequest;
import com.prm392.g5.labverse.dto.auth.LoginResponse;
import com.prm392.g5.labverse.dto.auth.LoginWGoogleRequest;
import com.prm392.g5.labverse.dto.auth.VerifyForgotPasswordOtpResponse;

import okhttp3.ResponseBody;
import retrofit2.Callback;

public class AuthRepository {
    private AuthApiService authApiService;

    public AuthRepository() {
        authApiService = RetrofitClient.getInstance().create(AuthApiService.class);
    }

    public void login(LoginRequest request, Callback<LoginResponse> callback){
        authApiService.login(request).enqueue(callback);
    }

    public void loginWGoogle(LoginWGoogleRequest request, Callback<LoginResponse> callback){
        authApiService.loginWGoogle(request).enqueue(callback);
    }

    public void forgotPassword(String email, Callback<ResponseBody> callback) {
        authApiService.forgotPassword(email).enqueue(callback);
    }

    public void verifyForgotPasswordOtp(String email, String otp, Callback<VerifyForgotPasswordOtpResponse> callback) {
        authApiService.verifyForgotPasswordOtp(email, otp).enqueue(callback);
    }

    public void resetPassword(String email, String resetPasswordToken, String newPassword, Callback<ResponseBody> callback) {
        authApiService.resetPassword(email, resetPasswordToken, newPassword).enqueue(callback);
    }

    public void resentForgotPasswordOtp(String email, Callback<ResponseBody> callback) {
        authApiService.resentForgotPasswordOtp(email).enqueue(callback);
    }

    public void logout(String token, Callback<ResponseBody> callback) {
        authApiService.logout("Bearer " + token).enqueue(callback);
    }
}
