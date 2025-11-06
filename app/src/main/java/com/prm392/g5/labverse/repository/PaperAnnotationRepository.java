package com.prm392.g5.labverse.repository;

import com.prm392.g5.labverse.apiService.PaperAnnotationApiService;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dto.S3SignedUrlResponse;
import com.prm392.g5.labverse.dto.paperAnnotation.AddPaperAnnotationRequest;
import com.prm392.g5.labverse.dto.paperAnnotation.PaperAnnotationInfoResponse;

import retrofit2.Callback;

public class PaperAnnotationRepository {
    private PaperAnnotationApiService annotationApiService;

    public PaperAnnotationRepository() {
        this.annotationApiService = RetrofitClient.getInstance().create(PaperAnnotationApiService.class);
    }

    public void getAnnotationInfo(String userId, String paperId, Callback<PaperAnnotationInfoResponse> callback){
        annotationApiService.getAnnotationInfo(userId, paperId).enqueue(callback);
    }

    public void getDownloadUrl(String s3Key, Callback<S3SignedUrlResponse> callback){
        annotationApiService.getAnnotationDownloadUrl(s3Key).enqueue(callback);
    }

    public void addOrUpdatePaperAnnotation(AddPaperAnnotationRequest request, Callback<PaperAnnotationInfoResponse> callback) {
        annotationApiService.addOrUpdatePaperAnnotation(request).enqueue(callback);
    }

    public void getUploadUrl(String s3key, Callback<S3SignedUrlResponse> callback){
        annotationApiService.getAnnotationUploadUrl(s3key).enqueue(callback);
    }
}
