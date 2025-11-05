package com.prm392.g5.labverse.activity.team;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.MemberAdapter;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dto.team.MemberResponse;
import com.prm392.g5.labverse.repository.TeamRepository;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamDetailActivity extends AppCompatActivity {

    private TextView tvTeamName, tvTeamDescription, tvEmptyMembers, tvEmptyReadingLists;
    private TextView tabMembers, tabReadingLists;
    private LinearLayout layoutMembers, layoutReadingLists;
    private RecyclerView rvMembers, rvReadingLists;
    private ProgressBar progressBar;
    private Button btnInviteMember;

    private MemberAdapter memberAdapter;
    private TeamRepository teamRepository;
    private List<MemberResponse> memberList = new ArrayList<>();

    private String teamId;
    private String teamName;
    private String teamDescription;
    private String createdBy;
    private boolean isOwner = false;

    private enum Tab {
        MEMBERS, READING_LISTS
    }

    private Tab currentTab = Tab.MEMBERS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        initViews();
        setupToolbar();
        getIntentData();
        checkOwnership();
        setupTabs();
        loadMembers();

        if (isOwner) {
            btnInviteMember.setVisibility(View.VISIBLE);
        }

        btnInviteMember.setOnClickListener(v -> {
            Toast.makeText(this, "Invite member feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvTeamName = findViewById(R.id.tvTeamName);
        tvTeamDescription = findViewById(R.id.tvTeamDescription);
        tvEmptyMembers = findViewById(R.id.tvEmptyMembers);
        tvEmptyReadingLists = findViewById(R.id.tvEmptyReadingLists);

        tabMembers = findViewById(R.id.tabMembers);
        tabReadingLists = findViewById(R.id.tabReadingLists);

        layoutMembers = findViewById(R.id.layoutMembers);
        layoutReadingLists = findViewById(R.id.layoutReadingLists);

        rvMembers = findViewById(R.id.rvMembers);
        rvReadingLists = findViewById(R.id.rvReadingLists);

        progressBar = findViewById(R.id.progressBar);
        btnInviteMember = findViewById(R.id.btnInviteMember);

        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvReadingLists.setLayoutManager(new LinearLayoutManager(this));

        teamRepository = new TeamRepository();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    private void getIntentData() {
        teamId = getIntent().getStringExtra("TEAM_ID");
        teamName = getIntent().getStringExtra("TEAM_NAME");
        teamDescription = getIntent().getStringExtra("TEAM_DESCRIPTION");
        createdBy = getIntent().getStringExtra("CREATED_BY");

        if (teamId == null) {
            Toast.makeText(this, "Missing team ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTeamName.setText(teamName != null ? teamName : "Unknown Team");
        tvTeamDescription.setText(teamDescription != null ? teamDescription : "No description");
    }

    private void checkOwnership() {
        String currentUserId = SharePreferenceManager.getInstance().getUserId();
        isOwner = currentUserId != null && currentUserId.equals(createdBy);
    }

    private void setupTabs() {
        tabMembers.setOnClickListener(v -> switchTab(Tab.MEMBERS));
        tabReadingLists.setOnClickListener(v -> switchTab(Tab.READING_LISTS));
    }

    private void switchTab(Tab tab) {
        currentTab = tab;

        if (tab == Tab.MEMBERS) {
            tabMembers.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            tabMembers.setTypeface(null, android.graphics.Typeface.BOLD);
            tabMembers.setBackgroundResource(R.drawable.tab_selected);

            tabReadingLists.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
            tabReadingLists.setTypeface(null, android.graphics.Typeface.NORMAL);
            tabReadingLists.setBackgroundResource(R.drawable.tab_unselected);

            layoutMembers.setVisibility(View.VISIBLE);
            layoutReadingLists.setVisibility(View.GONE);

        } else {
            tabReadingLists.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            tabReadingLists.setTypeface(null, android.graphics.Typeface.BOLD);
            tabReadingLists.setBackgroundResource(R.drawable.tab_selected);

            tabMembers.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
            tabMembers.setTypeface(null, android.graphics.Typeface.NORMAL);
            tabMembers.setBackgroundResource(R.drawable.tab_unselected);

            layoutMembers.setVisibility(View.GONE);
            layoutReadingLists.setVisibility(View.VISIBLE);

            loadReadingLists();
        }
    }

    private void loadMembers() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyMembers.setVisibility(View.GONE);

        teamRepository.getTeamMembers(teamId, new Callback<List<MemberResponse>>() {
            @Override
            public void onResponse(Call<List<MemberResponse>> call, Response<List<MemberResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    memberList = response.body();

                    if (memberList.isEmpty()) {
                        tvEmptyMembers.setVisibility(View.VISIBLE);
                    } else {
                        memberAdapter = new MemberAdapter(memberList, isOwner);
                        memberAdapter.setOnMemberDeleteListener((member, position) -> {
                            String currentUserId = SharePreferenceManager.getInstance().getUserId();
                            if (member.getId().equals(currentUserId)) {
                                Toast.makeText(TeamDetailActivity.this,
                                        "Cannot remove yourself from the team", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            showRemoveMemberDialog(member, position);
                        });

                        rvMembers.setAdapter(memberAdapter);
                    }
                } else {
                    Toast.makeText(TeamDetailActivity.this,
                            "Failed to load members", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MemberResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TeamDetailActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadReadingLists() {
        tvEmptyReadingLists.setVisibility(View.VISIBLE);
        tvEmptyReadingLists.setText("Reading lists feature coming soon");
    }

    private void showRemoveMemberDialog(MemberResponse member, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Member")
                .setMessage("Are you sure you want to remove " + member.getFullName() + " from this team?")
                .setPositiveButton("Remove", (dialog, which) -> removeMember(member.getId(), position))
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
                    memberList.remove(position);
                    memberAdapter.notifyItemRemoved(position);
                    Toast.makeText(TeamDetailActivity.this,
                            "Member removed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TeamDetailActivity.this,
                            "Failed to remove member", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TeamDetailActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isOwner) {
            getMenuInflater().inflate(R.menu.menu_team_detail, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            openEditTeamActivity();
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteTeamDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openEditTeamActivity() {
        Intent intent = new Intent(this, EditTeamActivity.class);
        intent.putExtra("TEAM_ID", teamId);
        intent.putExtra("TEAM_NAME", teamName);
        intent.putExtra("TEAM_DESCRIPTION", teamDescription);
        startActivityForResult(intent, 100);
    }

    private void showDeleteTeamDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Team")
                .setMessage("Are you sure you want to delete this team? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteTeam())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTeam() {
        progressBar.setVisibility(View.VISIBLE);

        teamRepository.deleteTeam(teamId, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);

                android.util.Log.d("DELETE_TEAM", "Response code: " + response.code());

                if (response.isSuccessful()) {
                    Toast.makeText(TeamDetailActivity.this,
                            "Team deleted successfully", Toast.LENGTH_SHORT).show();

                    new android.os.Handler().postDelayed(() -> {
                        setResult(RESULT_OK);
                        finish();
                    }, 300);
                } else {
                    Toast.makeText(TeamDetailActivity.this,
                            "Failed to delete team", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                android.util.Log.e("DELETE_TEAM", "Error", t);
                Toast.makeText(TeamDetailActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            teamName = data.getStringExtra("TEAM_NAME");
            teamDescription = data.getStringExtra("TEAM_DESCRIPTION");
            tvTeamName.setText(teamName);
            tvTeamDescription.setText(teamDescription);
        }
    }
}