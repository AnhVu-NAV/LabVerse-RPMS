package com.prm392.g5.labverse.util;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class S3Util {

    private static final OkHttpClient client = new OkHttpClient();

    public static void uploadPdfToS3(String presignUrl, File file, UploadCallback callback){
        uploadFileToS3(presignUrl, file, "application/pdf", callback);
    }

    private static void uploadFileToS3(String presignedUrl, File file, String contentType, UploadCallback callback) {
        RequestBody requestBody = RequestBody.create(file, MediaType.parse(contentType));
        Request request = new Request.Builder()
                .url(presignedUrl)
                .put(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(new IOException("Upload failed, code: " + response.code()));
                }
                response.close();
            }
        });
    }

    public interface UploadCallback {
        void onSuccess();
        void onError(Exception e);
    }
}
