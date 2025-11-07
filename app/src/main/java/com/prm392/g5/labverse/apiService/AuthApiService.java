package com.prm392.g5.labverse.apiService;

import com.prm392.g5.labverse.dto.auth.LoginRequest;
import com.prm392.g5.labverse.dto.auth.LoginResponse;
import com.prm392.g5.labverse.dto.auth.LoginWGoogleRequest;
import com.prm392.g5.labverse.dto.auth.VerifyForgotPasswordOtpResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface AuthApiService {
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/api/auth/google")
    Call<LoginResponse> loginWGoogle(@Body LoginWGoogleRequest request);

    @GET("/api/auth/forgot-pass")
    Call<ResponseBody> forgotPassword(@Query("email") String email);

    @POST("/api/auth/verify-forgot-password-otp")
    Call<VerifyForgotPasswordOtpResponse> verifyForgotPasswordOtp(
            @Query("email") String email,
            @Query("otp") String otp
    );

    @PUT("/api/auth/reset-password")
    Call<ResponseBody> resetPassword(
            @Query("email") String email,
            @Query("resetPasswordToken") String resetPasswordToken,
            @Query("newPassword") String newPassword
    );

    @GET("/api/auth/resent-forgot-password-otp")
    Call<ResponseBody> resentForgotPasswordOtp(@Query("email") String email);

    @POST("/api/auth/logout")
    Call<ResponseBody> logout(@Header("Authorization") String token);

}
