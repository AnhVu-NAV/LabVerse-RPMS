package com.prm392.g5.labverse.activity.team;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.MemberAdapter;
import com.prm392.g5.labverse.dto.team.MemberResponse;
import com.prm392.g5.labverse.repository.TeamRepository;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListMemberActivity extends AppCompatActivity {

    private RecyclerView rvMembers;
    private ProgressBar progressBar;
    private MemberAdapter adapter;
    private TeamRepository teamRepository;
    private String teamId;
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        rvMembers = findViewById(R.id.rvMembers);
        progressBar = findViewById(R.id.progressBar);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));

        teamId = getIntent().getStringExtra("TEAM_ID");
        isOwner = getIntent().getBooleanExtra("IS_OWNER", false);

        // Initialize repository
        teamRepository = new TeamRepository();

        // Load members
        loadMembers();
    }

    private void loadMembers() {
        progressBar.setVisibility(View.VISIBLE);

        teamRepository.getTeamMembers(teamId, new Callback<List<MemberResponse>>() {
            @Override
            public void onResponse(Call<List<MemberResponse>> call, Response<List<MemberResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<MemberResponse> members = response.body();

                    adapter = new MemberAdapter(members, isOwner, new MemberAdapter.OnMemberActionListener() {
                        @Override
                        public void onRemoveMember(MemberResponse member, int position) {
                            showRemoveMemberDialog(member, position);
                        }
                    });

                    rvMembers.setAdapter(adapter);
                } else {
                    Toast.makeText(ListMemberActivity.this,
                            "Failed to load members", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MemberResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ListMemberActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showRemoveMemberDialog(MemberResponse member, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Member")
                .setMessage("Are you sure you want to remove " + member.getFullName() + " from the team?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    removeMember(member.getId(), position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeMember(String memberId, int position) {
        progressBar.setVisibility(View.VISIBLE);

        teamRepository.removeMember(teamId, memberId, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(ListMemberActivity.this,
                            "Member removed successfully", Toast.LENGTH_SHORT).show();

                    // Reload members
                    loadMembers();
                } else {
                    Toast.makeText(ListMemberActivity.this,
                            "Failed to remove member", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ListMemberActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}