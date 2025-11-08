package com.prm392.g5.labverse.apiService;

import com.prm392.g5.labverse.dto.user.RegisterAccountRequest;
import com.prm392.g5.labverse.dto.user.UserSimpleResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface UserApiService {

    @POST("/api/accounts/register")
    Call<UserSimpleResponse> registerAccount(@Body RegisterAccountRequest request);

    @GET("/api/accounts/resent-otp-verify-account")
    Call<ResponseBody> resendOtpVerifyAccount(@Query("email") String email);

    @PUT("/api/accounts/verify-account")
    Call<ResponseBody> verifyAccount(
            @Query("email") String email,
            @Query("otp") String otp
    );

    @PUT("/api/accounts/select-role")
    Call<UserSimpleResponse> selectRole(@Query("userId") String userId, @Query("roleName") String roleName);
}
