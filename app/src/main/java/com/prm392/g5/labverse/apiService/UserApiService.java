package com.prm392.g5.labverse.apiService;

import com.prm392.g5.labverse.dto.user.RegisterAccountRequest;
import com.prm392.g5.labverse.dto.user.UserSimpleResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserApiService {

    @POST("/api/accounts/register")
    Call<UserSimpleResponse> registerAccount(@Body RegisterAccountRequest request);
}
