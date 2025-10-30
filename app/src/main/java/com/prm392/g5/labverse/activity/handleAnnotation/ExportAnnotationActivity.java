package com.prm392.g5.labverse.activity.handleAnnotation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.prm392.g5.labverse.LabVerse;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dto.ErrorResponse;
import com.prm392.g5.labverse.dto.S3SignedUrlResponse;
import com.prm392.g5.labverse.dto.paperAnnotation.PaperAnnotationInfoResponse;
import com.prm392.g5.labverse.entity.PaperAnnotation;
import com.prm392.g5.labverse.repository.PaperAnnotationRepository;
import com.prm392.g5.labverse.util.AnnotationHelper;

import java.io.File;
import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

public class ExportAnnotationActivity extends AppCompatActivity {

    private final PaperAnnotationRepository annotationRepository = new PaperAnnotationRepository();
    private final AnnotationHelper annotationHelper = new AnnotationHelper();
    private static final String PAPER_ID = "paper_id";

    public static void open(Context context, String paperId) {
        new AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_export_annotation)
                .setTitle("Export Annotation")
                .setMessage("Do you want to exporting annotation to a file?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(context, ExportAnnotationActivity.class);
                    intent.putExtra(PAPER_ID, paperId);
                    context.startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String paperId = getIntent().getStringExtra(PAPER_ID);
        String userId = SharePreferenceManager.getInstance().getUserId();
        annotationRepository.getAnnotationInfo(userId, paperId, new Callback<PaperAnnotationInfoResponse>() {
            @Override
            public void onResponse(Call<PaperAnnotationInfoResponse> call, Response<PaperAnnotationInfoResponse> response) {
                //nhận được response từ server
                processAnnotationResponse(userId, paperId, response);

            }
            @Override
            public void onFailure(Call<PaperAnnotationInfoResponse> call, Throwable t) {
                //todo khả năng ở đây phải hiện option cho người ta, retry hay là dùng bản offline
                // request thất bại -> xử lý như remote không tồn tại
                processAnnotationResponse(userId, paperId, null);
            }
        });
    }

    private void processAnnotationResponse(String userId, String paperId, Response<PaperAnnotationInfoResponse> response){
        AppDatabase.databaseWriteExecutor.execute(() -> {

            final AtomicBoolean exportFromServer = new AtomicBoolean(false);

            PaperAnnotationInfoResponse remoteInfo = null;
            boolean existInRemote = false;
            if (response != null){
                //có file trên remote
                existInRemote = response.isSuccessful() && response.body() != null;
                if (existInRemote) remoteInfo = response.body();
            }
            PaperAnnotation localAnnotation = AppDatabase.getInstance(this)
                    .paperAnnotationDao()
                    .getAnnotationByPaperIdAndUserId(paperId, userId);
            boolean existInLocal = (localAnnotation != null);

            if (!existInRemote && !existInLocal) {
                // Case 1: chưa có ở đâu cả -> hiện thông báo cho user la ko export duoc do khong co file
                new AlertDialog.Builder(this)
                        .setTitle("Export Annotation")
                        .setMessage("You are not having any annotation for this file, can not export!")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
                //muốn nếu vào th này thì kết thúc luôn
            } else if (!existInRemote ) {
                // Case 2: local có, remote chưa -> expot tu local ra
                exportLocalAnnotation(localAnnotation);

            } else if (!existInLocal) {
                // Case 3: remote có, local chưa -> tải về
                getAnnotationDownloadUrl(remoteInfo.getAnnotationS3Key());
            } else {
                // Case 4: cả hai đều có -> so sánh thời gian cập nhật de xem export cai nao
                Instant localTime = Instant.parse(localAnnotation.getUpdatedAt());
                Instant remoteTime = Instant.parse(remoteInfo.getUpdateAt());
                if (remoteTime.isAfter(localTime)) {
                    // Remote mới hơn -> tải về
                    getAnnotationDownloadUrl(remoteInfo.getAnnotationS3Key());
                } else {
                    // Local mới hơn hoặc giống remote -> export từ local
                    exportLocalAnnotation(localAnnotation);
                }
            }
        });
    }

    private void exportLocalAnnotation(PaperAnnotation localAnnotation) {
        File localAnnotationFile = new File(getExternalFilesDir(null), localAnnotation.getAnnotationS3Key());
        annotationHelper.exportAnnotationFromLocalToFile(localAnnotationFile, new AnnotationHelper.ExportAnnotationCallback() {
            @Override
            public void onSuccess(File exportedFile) {
                showShareDialog(exportedFile);
            }

            @Override
            public void onFail() {
                showExportFailDialog();
            }
        });
    }

    private void getAnnotationDownloadUrl(String s3Key) {
        annotationRepository.getDownloadUrl(s3Key, new Callback<S3SignedUrlResponse>() {
            @Override
            public void onResponse(Call<S3SignedUrlResponse> call, Response<S3SignedUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().getUrl();
                    annotationHelper.exportAnnotationFromServerToFile(url, new AnnotationHelper.ExportAnnotationCallback() {
                        @Override
                        public void onSuccess(File exportedFile) {
                            showShareDialog(exportedFile);
                        }

                        @Override
                        public void onFail() {
                            showExportFailDialog();
                        }
                    });
                } else {
                    handleErrorResponseFromBackend(response);
                }
            }
            @Override
            public void onFailure(Call<S3SignedUrlResponse> call, Throwable t) {
                handleSendRequestFail(t);
            }
        });
    }

    private void showExportFailDialog(){
        new AlertDialog.Builder(ExportAnnotationActivity.this)
                .setTitle("Export Annotation")
                .setMessage("There is error during export annotation. Please try later")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void showShareDialog(File file) {
        Uri uri = FileProvider.getUriForFile(
                ExportAnnotationActivity.this,
                "com.prm392.g5.labverse.provider",
                file
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/json");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        new AlertDialog.Builder(ExportAnnotationActivity.this)
                .setTitle("Export annotation successfully")
                .setMessage("File is saved at:\n" + file.getAbsolutePath())
                .setPositiveButton("Share", (dialog, which) -> {
                    startActivity(Intent.createChooser(shareIntent, "Share annotation"));
                })
                .setNegativeButton("Close", ((dialog, which) -> finish()))
                .show();

    }

    //đẩy mấy cái xử lí lỗi này ra class khác
    private void handleErrorResponseFromBackend(Response<?> response) {
        try {
            Converter<ResponseBody, ErrorResponse> converter =
                    RetrofitClient.getInstance()
                            .responseBodyConverter(ErrorResponse.class, new Annotation[0]);

            ErrorResponse errorResponse = converter.convert(response.errorBody());

            if (errorResponse != null) {
                Log.e("API_ERROR", "Code: " + errorResponse.getCode() + ", Message: " + errorResponse.getMessage());
                Toast.makeText(LabVerse.getInstance(),
                        "Error " + errorResponse.getCode() + ": " + errorResponse.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Log.e("API_ERROR", "Lỗi không rõ định dạng JSON.");
            }

        } catch (Exception e) {
            Log.e("API_ERROR", "Không parse được lỗi: " + e.getMessage(), e);
        }
    }

    private void handleSendRequestFail(Throwable t) {
        Log.e("Login", "Request failed", t);

        //xem xem co phai chuyeen ve main thread khoong
        if (t instanceof java.net.UnknownHostException) {
            Toast.makeText(LabVerse.getInstance(), "Không có kết nối mạng. Vui lòng kiểm tra Internet.", Toast.LENGTH_LONG).show();
        } else if (t instanceof java.net.SocketTimeoutException) {
            Toast.makeText(LabVerse.getInstance(), "Kết nối bị hết hạn. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
        } else if (t instanceof java.net.ConnectException) {
            Toast.makeText(LabVerse.getInstance(), "Không thể kết nối tới máy chủ.", Toast.LENGTH_LONG).show();
        } else if (t instanceof javax.net.ssl.SSLException) {
            Toast.makeText(LabVerse.getInstance(), "Lỗi chứng chỉ bảo mật.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(LabVerse.getInstance(), "Đã xảy ra lỗi không xác định. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
        }
    }

}