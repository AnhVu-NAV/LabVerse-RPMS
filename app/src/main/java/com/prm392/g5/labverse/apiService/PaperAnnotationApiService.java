package com.prm392.g5.labverse.apiService;

import com.prm392.g5.labverse.dto.S3SignedUrlResponse;
import com.prm392.g5.labverse.dto.paperAnnotation.AddPaperAnnotationRequest;
import com.prm392.g5.labverse.dto.paperAnnotation.PaperAnnotationInfoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PaperAnnotationApiService {

    @GET("/api/paperAnnotations")
    Call<PaperAnnotationInfoResponse> getAnnotationInfo (@Query("userId") String userId, @Query("paperId") String paperId);

    @GET("/api/paperAnnotations/downloadUrl")
    Call<S3SignedUrlResponse> getAnnotationDownloadUrl(@Query("s3Key") String s3Key);

    @POST("/api/paperAnnotations")
    Call<PaperAnnotationInfoResponse> addOrUpdatePaperAnnotation(@Body AddPaperAnnotationRequest request);

    @GET("/api/paperAnnotations/uploadUrl")
    Call<S3SignedUrlResponse> getAnnotationUploadUrl(@Query("s3Key") String s3Key);

}
