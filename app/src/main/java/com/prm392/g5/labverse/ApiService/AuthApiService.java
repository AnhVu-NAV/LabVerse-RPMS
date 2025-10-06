package com.prm392.g5.labverse.ApiService;

import com.prm392.g5.labverse.dto.auth.LoginRequest;
import com.prm392.g5.labverse.dto.auth.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
