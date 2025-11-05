package com.prm392.g5.labverse.repository;


import com.prm392.g5.labverse.apiService.TeamApiService;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dto.team.MemberResponse;
import com.prm392.g5.labverse.dto.team.TeamRequest;
import com.prm392.g5.labverse.dto.team.TeamResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Callback;

public class TeamRepository {
    private final TeamApiService teamApiService;

    public TeamRepository() {
        teamApiService = RetrofitClient.getInstance().create(TeamApiService.class);
    }

    public void getListTeamOfPi(Callback<List<TeamResponse>> callback) {
        String userId = SharePreferenceManager.getInstance().getUserId();
        teamApiService.getMyTeams(userId).enqueue(callback);
    }

    public void getTeamMembers(String teamId, Callback<List<MemberResponse>> callback) {
        teamApiService.getTeamMembers(teamId).enqueue(callback);
    }

    public void createTeam(TeamRequest teamRequest, Callback<TeamResponse> callback) {
        teamApiService.createTeam(teamRequest).enqueue(callback);
    }


    public void updateTeam(String teamId, TeamRequest teamRequest, Callback<TeamResponse> callback) {
        teamApiService.updateTeam(teamId, teamRequest).enqueue(callback);
    }

    public void deleteTeam(String teamId, Callback<ResponseBody> callback) {
        teamApiService.deleteTeam(teamId).enqueue(callback);
    }

    public void removeMember(String teamId, String memberId, Callback<ResponseBody> callback) {
        teamApiService.removeMember(teamId, memberId).enqueue(callback);
    }
}