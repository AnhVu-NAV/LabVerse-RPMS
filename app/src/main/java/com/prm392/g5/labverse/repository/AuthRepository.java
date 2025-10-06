package com.prm392.g5.labverse.repository;

import com.prm392.g5.labverse.ApiService.AuthApiService;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dto.auth.LoginRequest;
import com.prm392.g5.labverse.dto.auth.LoginResponse;

import retrofit2.Callback;

public class AuthRepository {
    private AuthApiService authApiService;

    public AuthRepository() {
        authApiService = RetrofitClient.getInstance().create(AuthApiService.class);
    }

    public void login(LoginRequest request, Callback<LoginResponse> callback){
        authApiService.login(request).enqueue(callback);
    }
}
