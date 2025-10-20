package com.prm392.g5.labverse.apiService;

import com.prm392.g5.labverse.dto.team.MemberResponse;
import com.prm392.g5.labverse.dto.team.TeamResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
public interface TeamApiService {
    @GET("/api/team/my-teams")
    Call<List<TeamResponse>> getMyTeams();

    @GET("/api/{teamId}/members")
    Call<List<MemberResponse>> getTeamMembers(@Path("teamId") String teamId);


}
