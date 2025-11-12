package com.prm392.g5.labverse.apiService;

import com.prm392.g5.labverse.dto.S3SignedUrlResponse;
import com.prm392.g5.labverse.dto.paper.AddPaperRequest;
import com.prm392.g5.labverse.dto.paper.AddPaperResponse;
import com.prm392.g5.labverse.dto.paper.PaperInfoResponse;
import com.prm392.g5.labverse.dto.paper.PageResponse;
import com.prm392.g5.labverse.dto.paper.PaperCardDto;
import com.prm392.g5.labverse.dto.paper.PaperSummaryDTO;
import com.prm392.g5.labverse.entity.Paper;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
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

    // ===== Dashboard =====
    // GET /api/papers?userId=&filter=&page=&size=
    @GET("/api/papers")
    Call<PageResponse<PaperCardDto>> listPapers(
            @Query("userId") String userId,
            @Query("filter") String filter,
            @Query("page") int page,
            @Query("size") int size
    );

    // PATCH /api/papers/{paperId}/progress?userId=
    @PATCH("/api/papers/{paperId}/progress")
    Call<Void> updateProgress(
            @Path("paperId") String paperId,
            @Query("userId") String userId,
            @Body UpdateProgressBody body
    );

    // POST & DELETE favorite
    @POST("/api/papers/{paperId}/favorite")
    Call<Void> addFavorite(@Path("paperId") String paperId, @Query("userId") String userId);

    @DELETE("/api/papers/{paperId}/favorite")
    Call<Void> removeFavorite(@Path("paperId") String paperId, @Query("userId") String userId);

    class UpdateProgressBody { public int currentPage; public UpdateProgressBody(int p){ this.currentPage=p; } }

    @GET("/api/papers/all")
    Call<List<PaperSummaryDTO>> getAllPapersForCurrentUser();

    @GET("api/papers/my-papers")
    Call<List<PaperInfoResponse>> getMyPapers();
}
