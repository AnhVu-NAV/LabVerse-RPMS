package com.prm392.g5.labverse.util;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnotationHelper {

//    private final PdfDocument document;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private PaperAnnotationRepository annotationRepository = new PaperAnnotationRepository();
    public AnnotationHelper() {}

    /** Import annotation từ file JSON local và overlay lên document */
    public void importFromLocal(File jsonFile, PdfDocument document) {
        if (jsonFile == null || !jsonFile.exists()) {
            Log.w("AnnotationHelper", "No local annotation file found.");
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

                Log.d("ANNOTATION_OVERLAY", "Imported annotation from local file.");
            } catch (Exception e) {
                Log.e("ANNOTATION_OVERLAY", "Import local failed", e);
                //todo cái này phải cho thử lại chứ nhỉ, ko nó sẽ bị ghi đè mất khi người dùng annotate mới
                Toast.makeText(LabVerse.getInstance(), "Fail to overlay annotation.", Toast.LENGTH_LONG).show();
            }
        });
    }
    public void updateAnnotation(File jsonFile, PaperAnnotation paperAnnotation, PdfDocument document) {
        //Ghi toàn bộ annotation hiện có ra file JSON local
        executor.execute(() -> {
            try {
                List<Annotation> annotations = document.getAnnotationProvider()
                        .getAllAnnotationsOfType(EnumSet.allOf(AnnotationType.class));
                JSONArray jsonArray = new JSONArray();
                for (Annotation a : annotations) {
                    jsonArray.put(a.toInstantJson());
                }

                try (FileWriter fw = new FileWriter(jsonFile, false)) {
                    fw.write(jsonArray.toString());
                }

                Log.d("ANNOTATION_UPDATE", "Exported annotation to local: " + jsonFile.getPath());
                //upload annotation to remote storage
                uploadToRemote(jsonFile, paperAnnotation);

            } catch (Exception e) {
                Log.e("ANNOTATION_UPDATE", "Export local failed", e);
                Toast.makeText(LabVerse.getInstance(), "Fail to update annotation.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void uploadToRemote(File jsonFile, PaperAnnotation paperAnnotation){
        annotationRepository.getUploadUrl(paperAnnotation.getAnnotationS3Key(), new Callback<S3SignedUrlResponse>() {
            @Override
            public void onResponse(Call<S3SignedUrlResponse> call, Response<S3SignedUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    String uploadUrl = response.body().getUrl();
                    S3Util.uploadJsonAnnotationToS3(uploadUrl, jsonFile, new S3Util.UploadCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d("ANNOTATION_UPLOAD", "Upload annotation to S3 thành công");
                            //add or update annotation in backend database and device database
                            updateAnnotationInDatabases(paperAnnotation);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("ANNOTATION_UPLOAD", "Upload annotation to S3 lỗi", e);
                            Toast.makeText(LabVerse.getInstance(), "Fail to update annotation. Unable to upload annotation", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<S3SignedUrlResponse> call, Throwable t) {
                Toast.makeText(LabVerse.getInstance(), "Fail to update annotation.", Toast.LENGTH_LONG).show();
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
                    Log.d("ANNOTATION_UPDATE", "update thành công annotation ở phía server");
                    Toast.makeText(LabVerse.getInstance(), "Sync annotation successfully.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LabVerse.getInstance(), "Fail to update annotation.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<PaperAnnotationInfoResponse> call, Throwable t) {
                Toast.makeText(LabVerse.getInstance(), "Fail to update annotation.", Toast.LENGTH_LONG).show();
            }
        });
    }


    /** Xuất file PDF đã embed annotation (bản sao của file gốc) */
    public void exportEmbeddedPdf(File originalPdf, File outputPdf,  PdfDocument document) {
        executor.execute(() -> {
            try {
                // Ghi annotation vào file gốc
                document.saveIfModified();

                FileUtil.copyContentFromTo(originalPdf, outputPdf);

                Log.d("AnnotationHelper", "Copied annotated PDF to: " + outputPdf.getPath());
            } catch (Exception e) {
                Log.e("AnnotationHelper", "Export embedded PDF failed", e);
            }
        });
    }

    public void exportAnnotationFromLocalToFile(File annotationFile, ExportAnnotationCallback callback){
        executor.execute(() -> {
            try {
                // Copy file sang thư mục Downloads mà user có thể truy cập
                File exportedFile = getExportedFile();

                //copy content to the exported file
                FileUtil.copyContentFromTo(annotationFile, exportedFile);
                Log.d("ANNOTATION_EXPORT", "Annotation exported from local file successfully.");
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(exportedFile));

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ANNOTATION_EXPORT", "Annotation exported from local file failed");
                new Handler(Looper.getMainLooper()).post(callback::onFail);
            }

        });
    }

    public void exportAnnotationFromServerToFile(String downloadUrl, ExportAnnotationCallback callback){

        File exportedFile = getExportedFile();

        S3Util.downloadFileFromS3(LabVerse.getInstance(), downloadUrl, exportedFile, new S3Util.DownloadCallback() {
            @Override
            public void onSuccess() {
                Log.d("ANNOTATION_EXPORT", "Annotation exported from server successfully.");
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(exportedFile));
            }

            @Override
            public void onError() {
                Log.e("ANNOTATION_EXPORT", "Annotation exported from server failed");
                new Handler(Looper.getMainLooper()).post(callback::onFail);
            }
        });
    }

    private File getExportedFile() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) downloadsDir.mkdirs();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return new File(downloadsDir, now + "_annotation.json");
    }

    public interface ExportAnnotationCallback{
        void onSuccess(File exportedFile);
        void onFail();
    }

    public void shutdown() {
        executor.shutdownNow();
    }

}