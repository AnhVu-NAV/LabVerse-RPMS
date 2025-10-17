package com.prm392.g5.labverse.config;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.prm392.g5.labverse.LabVerse;
import com.prm392.g5.labverse.activity.LoginActivity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
/**
 * Interceptor dùng để thêm header Authorization vào các request
 * xử lí thêm lỗi 401 nữa
 */
public class AuthInterceptor implements Interceptor {
    public AuthInterceptor() {
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String token = SharePreferenceManager.getInstance().getAccessToken();

        Request request = chain.request();
        if (token != null && !token.isEmpty()) {
            request = request.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
        }

        Response response = chain.proceed(request); //send request and receive response from server

        if (response.code() == 401){
            handleUnauthorized();
        }
        return response;
    }

    private void handleUnauthorized() {
        //xử lí lại lỗi nếu login sai

        // xóa token và user Id trên device
        SharePreferenceManager.getInstance().clearAccessToken();
        SharePreferenceManager.getInstance().clearUserId();

        //mở lại LoginActivity và xóa toàn bộ activity stack
        Intent intent = new Intent(LabVerse.getInstance(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //Chạy lại trên main thread
        new Handler(Looper.getMainLooper()).post(() -> { //chuyển veefe UI thread rồi mưới gọi start activity được
            LabVerse.getInstance().startActivity(intent);
        });
    }
}
