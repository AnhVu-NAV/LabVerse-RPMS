package com.prm392.g5.labverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.repository.AuthRepository;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharePreferenceManager sharePreferenceManager = SharePreferenceManager.getInstance();

        //gửi request logout đến server
        AuthRepository authRepository = new AuthRepository();
        authRepository.logout(sharePreferenceManager.getAccessToken(), new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(LogoutActivity.this, "Logout successful", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //todo xử lý khi logout thất bại, phải gửi lại access token về server
            }
        });

        //xóa hết dữ liệu đăng nhập trong SharedPreferences
        sharePreferenceManager.clearUserAuthData();

        //chuyển về màn login
        Intent intent = new Intent(LogoutActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }
}
