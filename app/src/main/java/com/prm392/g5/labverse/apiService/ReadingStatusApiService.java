package com.prm392.g5.labverse.apiService;

import com.prm392.g5.labverse.dto.readingStatus.ReadingStatusRequest;
import com.prm392.g5.labverse.dto.readingStatus.ReadingStatusResponse;
import com.prm392.g5.labverse.dto.team.TeamReadingStatusResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ReadingStatusApiService {

    @GET("/api/reading-status")
    Call<ReadingStatusResponse> getReadingStatusInfo(
            @Query("userId") String userId,
            @Query("paperId") String paperId
    );

    @POST("/api/reading-status/create-or-update")
    Call<ReadingStatusResponse> createOrUpdateReadingStatus(@Body ReadingStatusRequest request);

    @GET("/api/reading-status/team/{teamId}/paper/{paperId}")
    Call<List<TeamReadingStatusResponse>> getTeamReadingStatus(
            @Path("teamId") String teamId,
            @Path("paperId") String paperId
    );
}
