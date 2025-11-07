package com.prm392.g5.labverse.repository;

import com.prm392.g5.labverse.apiService.UserApiService;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dto.user.RegisterAccountRequest;
import com.prm392.g5.labverse.dto.user.UserSimpleResponse;

import retrofit2.Callback;

public class UserRepository {

    private final UserApiService userApiService;

    public UserRepository() {
        userApiService = RetrofitClient.getInstance().create(UserApiService.class);
    }

    public void registerAccount(RegisterAccountRequest request, Callback<UserSimpleResponse> callback) {
        userApiService.registerAccount(request).enqueue(callback);
    }

}
