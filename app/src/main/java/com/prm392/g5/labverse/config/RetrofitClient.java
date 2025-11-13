package com.prm392.g5.labverse.config;

import com.prm392.g5.labverse.apiService.SyncApiService;
import com.prm392.g5.labverse.repository.SyncRepository;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://10.0.2.2:8080";

    public static Retrofit getInstance() {
        if (retrofit == null) {
            // enable logging to Logcat
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); //change to other level, body just for dev

            //auth interceptor, attach access token to the header
            AuthInterceptor authInterceptor = new AuthInterceptor();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
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
