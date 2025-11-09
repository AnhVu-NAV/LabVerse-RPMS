package com.prm392.g5.labverse.repository;

import com.prm392.g5.labverse.apiService.AuthApiService;
import com.prm392.g5.labverse.apiService.PaperApiService;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dto.S3SignedUrlResponse;
import com.prm392.g5.labverse.dto.paper.AddPaperRequest;
import com.prm392.g5.labverse.dto.paper.AddPaperResponse;
import com.prm392.g5.labverse.dto.paper.PaperInfoResponse;

import retrofit2.Callback;

public class PaperRepository {

    private PaperApiService paperApiService;

    public PaperRepository() {
        paperApiService = RetrofitClient.getInstance().create(PaperApiService.class);
    }

    public void getUploadUrl(String s3Key, Callback<S3SignedUrlResponse> callback){
        paperApiService.getUploadUrl(s3Key).enqueue(callback);
    }

    public void addPaper(AddPaperRequest request, Callback<AddPaperResponse> callback){
        paperApiService.addPaper(request).enqueue(callback);
    }

    public void getPaperInfo(String id, Callback<PaperInfoResponse> callback){
        paperApiService.getPaperInfo(id).enqueue(callback);
    }

    public void getDownloadUrl(String s3Key, Callback<S3SignedUrlResponse> callback){
        paperApiService.getDownloadUrl(s3Key).enqueue(callback);
    }

}
