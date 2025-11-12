// com.prm392.g5.labverse.apiService.SyncApiService.java
package com.prm392.g5.labverse.apiService;

import com.prm392.g5.labverse.dto.sync.DeltaResponse;
import com.prm392.g5.labverse.dto.sync.PushResult;
import com.prm392.g5.labverse.entity.PaperAnnotation;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SyncApiService {
    @GET("/api/sync/{userId}/delta")
    Call<DeltaResponse> fetchDelta(@Path("userId") String userId,
                                   @Query("since") String since);

    @POST("/api/sync/{userId}/annotations")
    Call<PushResult> pushAnnotations(@Path("userId") String userId,
                                     @Body List<PaperAnnotation> annotations);
}
