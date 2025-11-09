package com.prm392.g5.labverse.repository;

import com.prm392.g5.labverse.apiService.InvitationApiService;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dto.team.InvitationRequest;
import com.prm392.g5.labverse.dto.team.InvitationResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Callback;

public class InvitationRepository {

    private final InvitationApiService invitationApiService;

    public InvitationRepository() {
        invitationApiService = RetrofitClient.getInstance().create(InvitationApiService.class);
    }

    // PI methods
    public void sendInvitation(InvitationRequest request, Callback<ResponseBody> callback) {
        invitationApiService.sendInvitation(request).enqueue(callback);
    }

    // User methods
    public void getMyInvitations(Callback<List<InvitationResponse>> callback) {
        invitationApiService.getMyInvitations().enqueue(callback);
    }

    public void acceptInvitation(String invitationId, Callback<ResponseBody> callback) {
        invitationApiService.acceptInvitation(invitationId).enqueue(callback);
    }

    public void rejectInvitation(String invitationId, Callback<ResponseBody> callback) {
        invitationApiService.rejectInvitation(invitationId).enqueue(callback);
    }
}