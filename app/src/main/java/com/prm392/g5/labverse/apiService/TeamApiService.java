package com.prm392.g5.labverse.apiService;

import com.prm392.g5.labverse.dto.team.MemberResponse;
import com.prm392.g5.labverse.dto.team.TeamReadingListRequest;
import com.prm392.g5.labverse.dto.team.TeamReadingListResponse;
import com.prm392.g5.labverse.dto.team.TeamRequest;
import com.prm392.g5.labverse.dto.team.TeamResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TeamApiService {

    @GET("api/team/my-teams")
    Call<List<TeamResponse>> getMyTeams(@Query("userId") String userId);

    @GET("api/team/{teamId}/members")
    Call<List<MemberResponse>> getTeamMembers(@Path("teamId") String teamId);

    @POST("api/team/create")
    Call<TeamResponse> createTeam(@Body TeamRequest teamRequest);


    @PUT("api/team/{teamId}")
    Call<TeamResponse> updateTeam(
            @Path("teamId") String teamId,
            @Body TeamRequest teamRequest
    );

    @DELETE("api/team/delete/{teamId}")
    Call<ResponseBody> deleteTeam(@Path("teamId") String teamId);

    @DELETE("api/team/{teamId}/members/{memberId}")
    Call<ResponseBody> removeMember(
            @Path("teamId") String teamId,
            @Path("memberId") String memberId
    );

    @GET("api/team/{teamId}/reading-lists")
    Call<List<TeamReadingListResponse>> getTeamReadingLists(
            @Path("teamId") String teamId
    );

    @GET("api/team/{teamId}/reading-lists/{readingListId}")
    Call<TeamReadingListResponse> getTeamReadingListById(
            @Path("teamId") String teamId,
            @Path("readingListId") String readingListId
    );

    @POST("api/team/{teamId}/create-reading-lists")
    Call<TeamReadingListResponse> createTeamReadingList(
            @Path("teamId") String teamId,
            @Body TeamReadingListRequest request
    );

    @PUT("api/team/{teamId}/update-reading-lists/{readingListId}")
    Call<TeamReadingListResponse> updateTeamReadingList(
            @Path("teamId") String teamId,
            @Path("readingListId") String readingListId,
            @Body TeamReadingListRequest request
    );

    @DELETE("api/team/{teamId}/delete-reading-lists/{readingListId}")
    Call<ResponseBody> deleteTeamReadingList(
            @Path("teamId") String teamId,
            @Path("readingListId") String readingListId
    );

}