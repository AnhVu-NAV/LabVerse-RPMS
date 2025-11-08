package com.prm392.g5.labverse.repository;


import com.prm392.g5.labverse.apiService.TeamApiService;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dto.team.MemberResponse;
import com.prm392.g5.labverse.dto.team.TeamReadingListRequest;
import com.prm392.g5.labverse.dto.team.TeamReadingListResponse;
import com.prm392.g5.labverse.dto.team.TeamRequest;
import com.prm392.g5.labverse.dto.team.TeamResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.http.Path;

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

    public void getReadingLists(String teamId, Callback<List<TeamReadingListResponse>> callback) {
        teamApiService.getTeamReadingLists(teamId).enqueue(callback);
    }

    public void getReadingListById(String teamId, String readingListId, Callback<TeamReadingListResponse> callback) {
        teamApiService.getTeamReadingListById(teamId, readingListId).enqueue(callback);
    }

    public void createReadingList(String teamId, TeamReadingListRequest request, Callback<TeamReadingListResponse> callback) {
        teamApiService.createTeamReadingList(teamId, request).enqueue(callback);
    }

    public void updateReadingList(String teamId, String readingListId, TeamReadingListRequest request, Callback<TeamReadingListResponse> callback) {
        teamApiService.updateTeamReadingList(teamId, readingListId, request).enqueue(callback);
    }
    public void deleteReadingList(String teamId, String readingListId, Callback<ResponseBody> callback) {
        teamApiService.deleteTeamReadingList(teamId, readingListId).enqueue(callback);
    }


}