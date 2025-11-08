package com.prm392.g5.labverse.apiService;

import com.prm392.g5.labverse.dto.team.InvitationRequest;
import com.prm392.g5.labverse.dto.team.InvitationResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface InvitationApiService {

    // PI endpoints
    @POST("api/invitations/pi/send")
    Call<ResponseBody> sendInvitation(@Body InvitationRequest request);

    @GET("api/invitations/pi/team/{teamId}")
    Call<List<InvitationResponse>> getTeamInvitations(@Path("teamId") String teamId);

    @POST("api/invitations/pi/{invitationId}/resend")
    Call<ResponseBody> resendInvitation(@Path("invitationId") String invitationId);

    @DELETE("api/invitations/pi/{invitationId}")
    Call<ResponseBody> cancelInvitation(@Path("invitationId") String invitationId);

    // User endpoints
    @GET("api/invitations/user/my-invitations")
    Call<List<InvitationResponse>> getMyInvitations();

    @POST("api/invitations/user/{invitationId}/accept")
    Call<ResponseBody> acceptInvitation(@Path("invitationId") String invitationId);

    @POST("api/invitations/user/{invitationId}/reject")
    Call<ResponseBody> rejectInvitation(@Path("invitationId") String invitationId);
}