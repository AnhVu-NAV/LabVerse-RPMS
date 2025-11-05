package com.prm392.g5.labverse.apiService;

import com.prm392.g5.labverse.dto.team.MemberResponse;
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
}