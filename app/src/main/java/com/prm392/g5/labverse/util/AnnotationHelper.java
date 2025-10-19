package com.prm392.g5.labverse.util;

import android.util.Log;
import android.widget.Toast;

import com.prm392.g5.labverse.LabVerse;
import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dao.PaperAnnotationDao;
import com.prm392.g5.labverse.dto.S3SignedUrlResponse;
import com.prm392.g5.labverse.dto.paperAnnotation.AddPaperAnnotationRequest;
import com.prm392.g5.labverse.dto.paperAnnotation.PaperAnnotationInfoResponse;
import com.prm392.g5.labverse.entity.PaperAnnotation;
import com.prm392.g5.labverse.repository.PaperAnnotationRepository;
import com.pspdfkit.annotations.Annotation;
import com.pspdfkit.annotations.AnnotationType;
import com.pspdfkit.document.PdfDocument;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnotationHelper {

    private final PdfDocument document;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private PaperAnnotationRepository annotationRepository = new PaperAnnotationRepository();
    public AnnotationHelper(PdfDocument document) {
        this.document = document;
    }

    /** Ghi toàn bộ annotation hiện có ra file JSON local */
    public void exportToLocal(File jsonFile) {
        executor.execute(() -> {
            try {
                List<Annotation> annotations = document.getAnnotationProvider()
                        .getAllAnnotationsOfType(EnumSet.allOf(AnnotationType.class));                JSONArray jsonArray = new JSONArray();
                for (Annotation a : annotations) {
                    jsonArray.put(a.toInstantJson());
                }

                try (FileWriter fw = new FileWriter(jsonFile, false)) {
                    fw.write(jsonArray.toString());
                }

                Log.d("AnnotationManager", "Exported annotation to local: " + jsonFile.getPath());
            } catch (Exception e) {
                Log.e("AnnotationManager", "Export local failed", e);
            }
        });
    }


    /** Import annotation từ file JSON local và overlay lên document */
    public void importFromLocal(File jsonFile) {
        if (jsonFile == null || !jsonFile.exists()) {
            Log.w("AnnotationManager", "No local annotation file found.");
            return;
        }
        executor.execute(() -> {
            try (FileInputStream fis = new FileInputStream(jsonFile)) {
                String content = new String(java.nio.file.Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8);
                JSONArray jsonArray = new JSONArray(content);

                for (int i = 0; i < jsonArray.length(); i++) {
                    String json = jsonArray.getString(i);
                    document.getAnnotationProvider().createAnnotationFromInstantJson(json);
                }

                Log.d("AnnotationManager", "Imported annotation from local file.");
            } catch (Exception e) {
                Log.e("AnnotationManager", "Import local failed", e);
            }
        });
    }
    public void uploadToRemote(File jsonFile, String s3Key){
        annotationRepository.getUploadUrl(s3Key, new Callback<S3SignedUrlResponse>() {
            @Override
            public void onResponse(Call<S3SignedUrlResponse> call, Response<S3SignedUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    String uploadUrl = response.body().getUrl();
                    S3Util.uploadJsonAnnotationToS3(uploadUrl, jsonFile, new S3Util.UploadCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d("ANNOTATION_UPLOAD", "Upload thành công");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("ANNOTATION_UPLOAD", "Upload lỗi", e);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<S3SignedUrlResponse> call, Throwable t) {
                Toast.makeText(LabVerse.getInstance(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateAnnotationInDatabases(PaperAnnotation paperAnnotation) {
        //luu db local truoc

        String now = Instant.now().toString();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            paperAnnotation.setUpdatedAt(now);
            PaperAnnotationDao dao = AppDatabase.getInstance(LabVerse.getInstance()).paperAnnotationDao();
            if (dao.getAnnotationById(paperAnnotation.getId()) == null){
                dao.insert(paperAnnotation);
            } else {
                dao.update(paperAnnotation);
            }
        });
        AddPaperAnnotationRequest request = new AddPaperAnnotationRequest();
        request.setId(paperAnnotation.getId());
        request.setUserId(SharePreferenceManager.getInstance().getUserId());
        request.setPaperId(paperAnnotation.getPaperId());
        request.setAnnotationS3Key(paperAnnotation.getAnnotationS3Key());
        request.setUpdateAt(now);
        annotationRepository.addOrUpdatePaperAnnotation(request, new Callback<PaperAnnotationInfoResponse>() {
            @Override
            public void onResponse(Call<PaperAnnotationInfoResponse> call, Response<PaperAnnotationInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    //update remote db
                    Log.d("ANNOTATION_UPDATE", "update thành công");
                } else {
                    Log.d("ANNOTATION_UPDATE", "update thất bại");
                }
            }
            @Override
            public void onFailure(Call<PaperAnnotationInfoResponse> call, Throwable t) {
                Toast.makeText(LabVerse.getInstance(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    /** Xuất file PDF đã embed annotation (bản sao của file gốc) */
    public void exportEmbeddedPdf(File originalPdf, File outputPdf) {
        executor.execute(() -> {
            try {
                // Ghi annotation vào file gốc
                document.saveIfModified();

                // Copy file PDF gốc (đã chứa annotation) ra vị trí mới
                try (InputStream in = new FileInputStream(originalPdf);
                     OutputStream out = new FileOutputStream(outputPdf)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0,  len);
                    }
                }

                Log.d("AnnotationManager", "Copied annotated PDF to: " + outputPdf.getPath());
            } catch (Exception e) {
                Log.e("AnnotationManager", "Export embedded PDF failed", e);
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }

}