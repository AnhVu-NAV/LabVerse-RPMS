package com.prm392.g5.labverse.activity.handleAnnotation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dao.PaperAnnotationDao;
import com.prm392.g5.labverse.dto.S3SignedUrlResponse;
import com.prm392.g5.labverse.dto.paperAnnotation.AddPaperAnnotationRequest;
import com.prm392.g5.labverse.dto.paperAnnotation.PaperAnnotationInfoResponse;
import com.prm392.g5.labverse.entity.PaperAnnotation;
import com.prm392.g5.labverse.repository.PaperAnnotationRepository;

import com.prm392.g5.labverse.util.FileUtil;
import com.prm392.g5.labverse.util.S3Util;

import java.io.File;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImportAnnotationActivity extends AppCompatActivity {

    private PaperAnnotationDao annotationDao;
    private final PaperAnnotationRepository annotationRepository = new PaperAnnotationRepository();
    private File uploadedFile;
    private static final String PAPER_ID = "paper_id";
    private boolean needToInsertToLocal = false;
    private PaperAnnotation paperAnnotation ;

    public static void open(Context context, String paperId) {
        new AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_import_annotation)
                .setTitle("Import Annotation")
                .setMessage("Once you import annotation, the existed annotation would be override!Be careful when picking file.")
                .setMessage("Do you want to import annotation to your paper's file?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(context, ImportAnnotationActivity.class);
                    intent.putExtra(PAPER_ID, paperId);
                    context.startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        annotationDao = AppDatabase.getInstance(this).paperAnnotationDao();

        String paperId = getIntent().getStringExtra(PAPER_ID);
        String userId = SharePreferenceManager.getInstance().getUserId();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            paperAnnotation = annotationDao.getAnnotationByPaperIdAndUserId(paperId, userId);

            if (paperAnnotation == null){
                needToInsertToLocal = true;
                //trước đó chưa có thông tin gì của annotation trong local
                paperAnnotation = new PaperAnnotation();
                paperAnnotation.setId();
                paperAnnotation.setUserId(userId);
                paperAnnotation.setPaperId(paperId);
                paperAnnotation.setAnnotationS3Key(userId + "/annotation/" + paperId + ".json");
            }
        });

        // Đăng ký launcher chọn file PDF
        ActivityResultLauncher<String> pickJsonFileLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        copyUriToTempFile(uri);
                    }
                });
        pickJsonFileLauncher.launch("application/json");
    }

    private void copyUriToTempFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            //the s3Key is similar to the location of the destination file
            uploadedFile = new File(getExternalFilesDir(null), paperAnnotation.getAnnotationS3Key());

            // Tạo thư mục cha nếu chưa có
            File parentDir = uploadedFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs(); // tạo đầy đủ cả cây thư mục
            }

            //viết ra file đích
            assert inputStream != null;
            FileUtil.copyContentFromTo(inputStream, uploadedFile);

            //file location được dùng làm s3 key luôn
            //hơi liều nhưng để xử lí nhanh thì đành làm thế
            uploadToS3();
        } catch (Exception e) {
            Log.e("IMPORT ANNOTATION", "Copy file lỗi", e);
            Toast.makeText(ImportAnnotationActivity.this, "There is error during import annotation. Please try again", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void uploadToS3() {

        if (uploadedFile == null) return;

        // get url để upload
        annotationRepository.getUploadUrl(paperAnnotation.getAnnotationS3Key(), new Callback<>() {
            @Override
            public void onResponse(Call<S3SignedUrlResponse> call, Response<S3SignedUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String uploadUrl = response.body().getUrl();
                    //upload lên url của s3
                    S3Util.uploadJsonAnnotationToS3(uploadUrl, uploadedFile, new S3Util.UploadCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d("ANNOTATION_UPLOAD", "Upload annotation to S3 thành công");
                            // tiếp tục xử lí gửi metadata cho remote, rồi lưu về local database
                            sendBackAnnotationMetadata();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("ANNOTATION_UPLOAD", "Error happen during upload file annotation to remote storage", e);
                            runOnUiThread(() -> Toast.makeText(ImportAnnotationActivity.this, "Error happen during upload file annotation to remote storage: " + e.getMessage(), Toast.LENGTH_LONG).show());
                            finish();
                        }
                    });
                } else {
                    Toast.makeText(ImportAnnotationActivity.this, "Lấy URL upload thất bại", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<S3SignedUrlResponse> call, Throwable t) {
                Toast.makeText(ImportAnnotationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void sendBackAnnotationMetadata() {
        paperAnnotation.setUpdatedAt();

        //aka add Paper object to server
        AddPaperAnnotationRequest requestDto = new AddPaperAnnotationRequest();
        requestDto.setId(paperAnnotation.getId());
        requestDto.setUserId(paperAnnotation.getUserId());
        requestDto.setPaperId(paperAnnotation.getPaperId());
        requestDto.setAnnotationS3Key(paperAnnotation.getAnnotationS3Key());
        requestDto.setUpdateAt(paperAnnotation.getUpdatedAt());

        annotationRepository.addOrUpdatePaperAnnotation(requestDto, new Callback<PaperAnnotationInfoResponse>() {
            @Override
            public void onResponse(Call<PaperAnnotationInfoResponse> call, Response<PaperAnnotationInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    //lưu object paper annotation vào trong local db neu can
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        if (needToInsertToLocal) {
                            annotationDao.insert(paperAnnotation);
                        } else {
                            annotationDao.update(paperAnnotation);
                        }
                    });
                    Log.d("ANNOTATION_IMPORT", "Saved annotation to local db: " + paperAnnotation.getPaperId());
                    Toast.makeText(ImportAnnotationActivity.this, "Import annotation successfully", Toast.LENGTH_SHORT).show();
                    finish();

                } else {
                    Toast.makeText(ImportAnnotationActivity.this, "There is error during import annotation. Please try again", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Call<PaperAnnotationInfoResponse> call, Throwable t) {
                Toast.makeText(ImportAnnotationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
