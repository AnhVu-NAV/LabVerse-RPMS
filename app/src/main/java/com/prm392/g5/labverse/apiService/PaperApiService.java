package com.prm392.g5.labverse.apiService;

import com.prm392.g5.labverse.dto.S3SignedUrlResponse;
import com.prm392.g5.labverse.dto.paper.AddPaperRequest;
import com.prm392.g5.labverse.dto.paper.AddPaperResponse;
import com.prm392.g5.labverse.dto.paper.PaperInfoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PaperApiService {

    @GET("/api/papers/uploadUrl")
    Call<S3SignedUrlResponse> getUploadUrl (@Query("key") String s3Key);

    @POST("/api/papers")
    Call<AddPaperResponse> addPaper (@Body AddPaperRequest request);


    @GET("/api/papers/downloadUrl")
    Call<S3SignedUrlResponse> getDownloadUrl(@Query("key") String s3Key);

    @GET("/api/papers/{id}")
    Call<PaperInfoResponse> getPaperInfo(@Path("id") String id);

}
