package com.prm392.g5.labverse.repository;

import com.prm392.g5.labverse.apiService.TeamApiService;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dto.team.MemberResponse;
import com.prm392.g5.labverse.dto.team.TeamResponse;

import java.util.List;

import retrofit2.Callback;

public class TeamRepository {
    private TeamApiService teamApiService;

    public TeamRepository() {
        teamApiService = RetrofitClient.getInstance().create(TeamApiService.class);
    }

    public void getListTeamOfPi(Callback<List<TeamResponse>> callback){
        teamApiService.getMyTeams().enqueue(callback);
    }

    public void getTeamMembers(String teamId, Callback<List<MemberResponse>> callback) {
        teamApiService.getTeamMembers(teamId).enqueue(callback);
    }

}
