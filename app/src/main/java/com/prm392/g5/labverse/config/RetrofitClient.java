package com.prm392.g5.labverse.config;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    //todo base url
    //todo xem có cách nào tự động gửi token đi không, sai thì xóa ở đt đi và vứt nó về login ngay
    private static final String BASE_URL = "http://10.0.2.2:8080";
    public static Retrofit getInstance() {
        if (retrofit == null) {
            // Logging interceptor (tùy chọn)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); //change to other level, body just for dev

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
