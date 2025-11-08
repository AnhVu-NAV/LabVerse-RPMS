package com.prm392.g5.labverse.activity.team;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.InvitationAdapter;
import com.prm392.g5.labverse.dto.team.InvitationResponse;
import com.prm392.g5.labverse.repository.InvitationRepository;
import com.prm392.g5.labverse.util.ApiErrorHandler;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyInvitationsActivity extends AppCompatActivity {

    private RecyclerView rvInvitations;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private InvitationAdapter adapter;
    private InvitationRepository invitationRepository;
    private List<InvitationResponse> invitationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_invitations);

        initViews();
        setupToolbar();
        loadInvitations();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvInvitations = findViewById(R.id.rvInvitations);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);

        rvInvitations.setLayoutManager(new LinearLayoutManager(this));

        invitationRepository = new InvitationRepository();

        adapter = new InvitationAdapter(invitationList, new InvitationAdapter.OnInvitationActionListener() {
            @Override
            public void onAcceptClick(InvitationResponse invitation, int position) {
                showAcceptDialog(invitation, position);
            }

            @Override
            public void onRejectClick(InvitationResponse invitation, int position) {
                showRejectDialog(invitation, position);
            }
        });

        rvInvitations.setAdapter(adapter);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadInvitations() {
        progressBar.setVisibility(View.VISIBLE);
        rvInvitations.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        invitationRepository.getMyInvitations(new Callback<List<InvitationResponse>>() {
            @Override
            public void onResponse(Call<List<InvitationResponse>> call, Response<List<InvitationResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    invitationList.clear();
                    invitationList.addAll(response.body());

                    if (invitationList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvInvitations.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    ApiErrorHandler.handleApiResponseError(MyInvitationsActivity.this, response, "LoadInvitations");
                }
            }

            @Override
            public void onFailure(Call<List<InvitationResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                ApiErrorHandler.handleNetworkFailure(MyInvitationsActivity.this, t, "LoadInvitations");
            }
        });
    }

    private void showAcceptDialog(InvitationResponse invitation, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Accept Invitation")
                .setMessage("Are you sure you want to join " + invitation.getTeamName() + "?")
                .setPositiveButton("Accept", (dialog, which) -> acceptInvitation(invitation, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRejectDialog(InvitationResponse invitation, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Decline Invitation")
                .setMessage("Are you sure you want to decline this invitation?")
                .setPositiveButton("Decline", (dialog, which) -> rejectInvitation(invitation, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void acceptInvitation(InvitationResponse invitation, int position) {
        progressBar.setVisibility(View.VISIBLE);

        invitationRepository.acceptInvitation(invitation.getId(), new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(MyInvitationsActivity.this,
                            "You've joined " + invitation.getTeamName(), Toast.LENGTH_SHORT).show();
                    invitationList.remove(position);
                    adapter.notifyItemRemoved(position);

                    if (invitationList.isEmpty()) {
                        rvInvitations.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    ApiErrorHandler.handleApiResponseError(MyInvitationsActivity.this, response, "AcceptInvitation");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ApiErrorHandler.handleNetworkFailure(MyInvitationsActivity.this, t, "AcceptInvitation");
            }
        });
    }

    private void rejectInvitation(InvitationResponse invitation, int position) {
        progressBar.setVisibility(View.VISIBLE);

        invitationRepository.rejectInvitation(invitation.getId(), new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(MyInvitationsActivity.this,
                            "Invitation declined", Toast.LENGTH_SHORT).show();
                    invitationList.remove(position);
                    adapter.notifyItemRemoved(position);

                    if (invitationList.isEmpty()) {
                        rvInvitations.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    ApiErrorHandler.handleApiResponseError(MyInvitationsActivity.this, response, "RejectInvitation");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ApiErrorHandler.handleNetworkFailure(MyInvitationsActivity.this, t, "RejectInvitation");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}