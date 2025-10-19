package com.prm392.g5.labverse.activity.readPaper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dao.PaperAnnotationDao;
import com.prm392.g5.labverse.dao.PaperDao;
import com.prm392.g5.labverse.dto.ErrorResponse;
import com.prm392.g5.labverse.dto.S3SignedUrlResponse;
import com.prm392.g5.labverse.dto.paper.PaperInfoResponse;
import com.prm392.g5.labverse.dto.paperAnnotation.PaperAnnotationInfoResponse;
import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.entity.PaperAnnotation;
import com.prm392.g5.labverse.repository.PaperAnnotationRepository;
import com.prm392.g5.labverse.repository.PaperRepository;
import com.prm392.g5.labverse.util.S3Util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

public class PrepareViewPdfActivity extends AppCompatActivity {

    private AppDatabase db;
    private PaperDao paperDao;
    private PaperAnnotationDao annotationDao;
    private PaperRepository paperRepository = new PaperRepository();
    private PaperAnnotationRepository annotationRepository = new PaperAnnotationRepository();
    private static final String PAPER_ID = "paper_id";
    private Paper wouldBeOpenedPaper;
    private PaperAnnotation wouldBeOpenedAnnotation;
    //todo file location, đang lưu ở external dir
    private File localPdfFile;
    private File localAnnotationFile;

    /**
     * Gọi hàm này từ Activity khác để mở chuẩn bị (tải file) trước khi mở pdf
     *
     * @param paperId truyền từ activity khac vaof ddeer mowr ra
     */
    public static void open(Context context, String paperId) {
        Intent intent = new Intent(context, PrepareViewPdfActivity.class);
        intent.putExtra(PAPER_ID, paperId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String paperId = getIntent().getStringExtra(PAPER_ID);
        String userId = SharePreferenceManager.getInstance().getUserId();

        if (paperId == null) {
            Toast.makeText(this, "Không có PDF key!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getInstance(this);
        paperDao = db.paperDao();
        annotationDao = db.paperAnnotationDao();


        // Chạy Room trong background thread
        AppDatabase.databaseWriteExecutor.execute(() -> {
            wouldBeOpenedPaper = paperDao.getById(paperId);
            //trước đó chưa từng có object paper sắp mở trong database (người dùng chưa mở file pdf cuar paper ra bao giờ)
            if (wouldBeOpenedPaper == null) {
                //lấy thông tin từ backend về
                paperRepository.getPaperInfo(paperId, new Callback<PaperInfoResponse>() {
                    @Override
                    public void onResponse(Call<PaperInfoResponse> call, Response<PaperInfoResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            PaperInfoResponse res = response.body();
                            //lưu thông tin vào local database
                            Paper newPaper = new Paper();
                            newPaper.setId(res.getId());
                            newPaper.setS3Key(res.getS3Key());
                            newPaper.setTotalPage(res.getTotalPage());
                            newPaper.setCurrentPage(res.getCurrentPage());
                            paperDao.insert(newPaper);
                            wouldBeOpenedPaper = newPaper;

                            runOnUiThread(() -> {
                                localPdfFile = new File(getExternalFilesDir(null), newPaper.getS3Key());
                                getPdfDownloadUrl();
                            });
                        } else {
                            handleErrorResponseFromBackend(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<PaperInfoResponse> call, Throwable t) {
                        handleSendRequestFail(t);
                    }
                });
            } else {
                runOnUiThread(() -> {
                    localPdfFile = new File(getExternalFilesDir(null), wouldBeOpenedPaper.getS3Key());
                    handleAnnotation(userId, paperId);
                });
            }
        });
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
                Toast.makeText(PrepareViewPdfActivity.this,
                        "Lỗi " + errorResponse.getCode() + ": " + errorResponse.getMessage(),
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
            Toast.makeText(PrepareViewPdfActivity.this, "Không có kết nối mạng. Vui lòng kiểm tra Internet.", Toast.LENGTH_LONG).show();
        } else if (t instanceof java.net.SocketTimeoutException) {
            Toast.makeText(PrepareViewPdfActivity.this, "Kết nối bị hết hạn. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
        } else if (t instanceof java.net.ConnectException) {
            Toast.makeText(PrepareViewPdfActivity.this, "Không thể kết nối tới máy chủ.", Toast.LENGTH_LONG).show();
        } else if (t instanceof javax.net.ssl.SSLException) {
            Toast.makeText(PrepareViewPdfActivity.this, "Lỗi chứng chỉ bảo mật.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(PrepareViewPdfActivity.this, "Đã xảy ra lỗi không xác định. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
        }
    }


    public void handleAnnotation(String userId, String paperId) {
        //get annotation info from server
        annotationRepository.getAnnotationInfo(userId, paperId, new Callback<PaperAnnotationInfoResponse>() {
            @Override
            public void onResponse(Call<PaperAnnotationInfoResponse> call, Response<PaperAnnotationInfoResponse> response) {
                //nhận được response từ server
                AppDatabase.databaseWriteExecutor.execute(() -> {

                    final AtomicBoolean needToDownload = new AtomicBoolean(false);

                    PaperAnnotationInfoResponse remoteInfo = null;
                    boolean existInRemote = response.isSuccessful() && response.body() != null;
                    if (existInRemote) remoteInfo = response.body();

                    PaperAnnotation localAnnotation = annotationDao.getAnnotationByPaperIdAndUserId(paperId, userId);
                    boolean existInLocal = (localAnnotation != null);

                    if (!existInRemote && !existInLocal) {
                        // Case 1: chưa có ở đâu cả → tạo mới local
                        wouldBeOpenedAnnotation = new PaperAnnotation();
                        wouldBeOpenedAnnotation.setId();
                        wouldBeOpenedAnnotation.setUserId(userId);
                        wouldBeOpenedAnnotation.setPaperId(paperId);
                        wouldBeOpenedAnnotation.setAnnotationS3Key(userId + "/annotation/" + paperId + ".json");
                        wouldBeOpenedAnnotation.setUpdatedAt(Instant.now().toString());
                        //để tới khi upload rồi hẵng thêm vào db
                    } else if (!existInRemote && existInLocal) {
                        // Case 2: local có, remote chưa → dùng local, chờ sync sau
                        wouldBeOpenedAnnotation = localAnnotation;
                    } else if (existInRemote && !existInLocal) {
                        // Case 3: remote có, local chưa → tải về
                        wouldBeOpenedAnnotation = new PaperAnnotation();
                        wouldBeOpenedAnnotation.setId(remoteInfo.getId());
                        wouldBeOpenedAnnotation.setUserId(userId);
                        wouldBeOpenedAnnotation.setPaperId(paperId);
                        wouldBeOpenedAnnotation.setAnnotationS3Key(remoteInfo.getAnnotationS3Key());
                        wouldBeOpenedAnnotation.setUpdatedAt(remoteInfo.getUpdateAt());
                        //tới khi upload ròi hẵng thêm vo db
                        needToDownload.set(true);
                    } else {
                        // Case 4: cả hai đều có → so sánh thời gian cập nhật
                        Instant localTime = Instant.parse(localAnnotation.getUpdatedAt());
                        Instant remoteTime = Instant.parse(remoteInfo.getUpdateAt());
                        if (remoteTime.isAfter(localTime)) {
                            // Remote mới hơn → tải về
                            wouldBeOpenedAnnotation = localAnnotation;
                            needToDownload.set(true);
                        } else {
                            // Local mới hơn hoặc giống remote → giữ local
                            wouldBeOpenedAnnotation = localAnnotation;
                        }
                    }

                    localAnnotationFile = new File(getExternalFilesDir(null), wouldBeOpenedAnnotation.getAnnotationS3Key());

                    runOnUiThread(() -> {
                        if (needToDownload.get()) {
                            getAnnotationDownloadUrl();
                        } else {
                            openPdf();
                        }
                    });
                });
            }

            //todo ddaasy  ra class khacs xu li loi
            @Override
            public void onFailure(Call<PaperAnnotationInfoResponse> call, Throwable t) {
                handleSendRequestFail(t);
            }
        });
    }

    private void getPdfDownloadUrl() {
        paperRepository.getDownloadUrl(wouldBeOpenedPaper.getS3Key(), new Callback<S3SignedUrlResponse>() {
            @Override
            public void onResponse(Call<S3SignedUrlResponse> call, Response<S3SignedUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().getUrl();
                    downloadPdf(url);
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

    private void getAnnotationDownloadUrl() {
        annotationRepository.getDownloadUrl(wouldBeOpenedAnnotation.getAnnotationS3Key(), new Callback<S3SignedUrlResponse>() {
            @Override
            public void onResponse(Call<S3SignedUrlResponse> call, Response<S3SignedUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().getUrl();
                    //todo
                    downLoadAnnotation(url);
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

    private void downloadPdf(String downloadUrl) {
        S3Util.downloadFileFromS3(this, downloadUrl, localPdfFile, new S3Util.DownloadCallback() {
            @Override
            public void onSuccess() {
                handleAnnotation(SharePreferenceManager.getInstance().getUserId(), wouldBeOpenedPaper.getId());
            }

            @Override
            public void onError() {
                Toast.makeText(PrepareViewPdfActivity.this, "Download file pdf thất bại, thử lại sau.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void downLoadAnnotation(String downloadUrl) {
        //taoj file tamj
        File tempFile = new File(getExternalFilesDir(null), wouldBeOpenedAnnotation.getAnnotationS3Key() + ".tmp");
        S3Util.downloadFileFromS3(this, downloadUrl, tempFile, new S3Util.DownloadCallback() {
            @Override
            public void onSuccess() {
                // ghi đè khi tải thành công
                if (tempFile.renameTo(localAnnotationFile)) {
                    Log.d("ANNOTATION_DOWNLOAD", "Annotation updated successfully.");
                } else {
                    Log.e("ANNOTATION_DOWNLOAD", "Rename failed, keeping old annotation.");
                }
                runOnUiThread(() -> openPdf());
            }

            @Override
            public void onError() {
                Log.e("ANNOTATION_DOWNLOAD", "Annotation download failed");
                Toast.makeText(PrepareViewPdfActivity.this, "Download file annotation  thất bại, bạn sẽ tiếp tục sử dụng phiên bản cũ.", Toast.LENGTH_LONG).show();
                tempFile.delete(); // xóa file lỗi
                runOnUiThread(() -> openPdf());
            }
        });
    }

    private void openPdf() {
        MyPdfActivity.open(this, wouldBeOpenedPaper, wouldBeOpenedAnnotation, localPdfFile, localAnnotationFile);
        finish();
    }

}
