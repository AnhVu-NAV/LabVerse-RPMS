package com.prm392.g5.labverse.util;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.prm392.g5.labverse.LabVerse;

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

    public static void uploadPdfToS3(String presignUrl, File pdfFile, UploadCallback callback){
        uploadFileToS3(presignUrl, pdfFile, "application/pdf", callback);
    }

    public static void uploadJsonAnnotationToS3(String presignUrl, File annotationFile, UploadCallback callback){
        uploadFileToS3(presignUrl, annotationFile, "application/json", callback);
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

    public static void downloadFileFromS3(Context context, String presignUrl, File destinationFile, DownloadCallback callback ){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(presignUrl));
        request.setTitle("Đang tải file về...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setDestinationUri(Uri.fromFile(destinationFile));

        DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        long downloadId = manager.enqueue(request);


        // Lắng nghe khi tải xong
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("DOWNLOAD_FROM_S3", "Broadcast received!");
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    // tải xong, kiểm tra status
                    try {
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(downloadId);
                        try (Cursor cursor = manager.query(query)) {
                            if (cursor.moveToFirst()) {
                                @SuppressLint("Range") int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    callback.onSuccess();
                                } else {
                                    Log.d("Download from s3", "download fail");
                                    callback.onError();
                                }
                            }
                        }
                    } finally {
                        try {
                            context.unregisterReceiver(this);
                        } catch (IllegalArgumentException ignored) {
                            // đã bị unregister rồi, bỏ qua
                        }
                    }
                }
            }
        };

        LabVerse.getInstance().registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED);
    }

    public interface DownloadCallback {
        void onSuccess();
        void onError();
    }

}
