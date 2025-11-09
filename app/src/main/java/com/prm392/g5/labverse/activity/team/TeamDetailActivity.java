package com.prm392.g5.labverse.activity.team;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.MemberAdapter;
// ❌ BỎ import ReadingListAdapter cũ
// import com.prm392.g5.labverse.adapter.ReadingListAdapter;
import com.prm392.g5.labverse.adapter.TeamReadingListAdapter;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dto.team.InvitationRequest;
import com.prm392.g5.labverse.dto.team.MemberResponse;
import com.prm392.g5.labverse.dto.team.TeamReadingListRequest;
import com.prm392.g5.labverse.dto.team.TeamReadingListResponse;
import com.prm392.g5.labverse.repository.InvitationRepository;
import com.prm392.g5.labverse.repository.TeamRepository;
import com.prm392.g5.labverse.util.ApiErrorHandler;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvTeamName, tvTeamDescription, tvMembersCount;
    private Button btnInviteMember, btnFilter;
    private HorizontalScrollView filterChipScrollView;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipApproved, chipPending, chipRejected;
    private RecyclerView rvMembers;
    private ProgressBar progressBar;

    private TextView tabMembers, tabReadingLists;
    private LinearLayout layoutMembers, layoutReadingLists;

    private MemberAdapter memberAdapter;
    private TeamRepository teamRepository;
    private InvitationRepository invitationRepository;
    private AlertDialog inviteDialog;
    private String teamId;
    private String currentUserId;
    private boolean isOwner;
    private boolean isFilterVisible = false;

    // --- Reading Lists ---
    private RecyclerView rvReadingLists;
    private TextView tvEmptyReadingLists;
    private FloatingActionButton fabCreateReadingList;
    private TeamReadingListAdapter teamReadingListsAdapter;
    private AlertDialog readingListDialog;

    private ActivityResultLauncher<Intent> editTeamLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TeamDetailActivity.this.setContentView(R.layout.activity_team_detail);
        registerEditTeamLauncher();

        initViews();
        setupToolbar();
        setupListeners();
        loadTeamDetails();
        loadMembers();
    }

    private void registerEditTeamLauncher() {
        editTeamLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String updatedName = result.getData().getStringExtra("UPDATED_TEAM_NAME");
                        String updatedDescription = result.getData().getStringExtra("UPDATED_TEAM_DESCRIPTION");

                        if (updatedName != null) tvTeamName.setText(updatedName);
                        if (updatedDescription != null) tvTeamDescription.setText(updatedDescription);

                        Toast.makeText(this, "Team updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTeamName = findViewById(R.id.tvTeamName);
        tvTeamDescription = findViewById(R.id.tvTeamDescription);
        tvMembersCount = findViewById(R.id.tvMembersCount);
        btnInviteMember = findViewById(R.id.btnInviteMember);
        btnFilter = findViewById(R.id.btnFilter);
        filterChipScrollView = findViewById(R.id.filterChipScrollView);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipAll = findViewById(R.id.chipAll);
        chipApproved = findViewById(R.id.chipApproved);
        chipPending = findViewById(R.id.chipPending);
        chipRejected = findViewById(R.id.chipRejected);
        rvMembers = findViewById(R.id.rvMembers);
        progressBar = findViewById(R.id.progressBar);

        tabMembers = findViewById(R.id.tabMembers);
        tabReadingLists = findViewById(R.id.tabReadingLists);
        layoutMembers = findViewById(R.id.layoutMembers);
        layoutReadingLists = findViewById(R.id.layoutReadingLists);

        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        teamRepository = new TeamRepository();
        invitationRepository = new InvitationRepository();

        teamId = getIntent().getStringExtra("TEAM_ID");
        currentUserId = SharePreferenceManager.getInstance().getUserId();

        // Reading Lists
        rvReadingLists = findViewById(R.id.rvReadingLists);
        tvEmptyReadingLists = findViewById(R.id.tvEmptyReadingLists);
        rvReadingLists.setLayoutManager(new LinearLayoutManager(this));
        fabCreateReadingList = findViewById(R.id.fabCreateReadingList);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Team Details");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        tabMembers.setOnClickListener(v -> switchToMembersTab());
        tabReadingLists.setOnClickListener(v -> switchToReadingListsTab());

        fabCreateReadingList.setOnClickListener(v -> showCreateReadingListDialog());

        btnFilter.setOnClickListener(v -> {
            isFilterVisible = !isFilterVisible;
            filterChipScrollView.setVisibility(isFilterVisible ? View.VISIBLE : View.GONE);
            btnFilter.setText(isFilterVisible ? "Filter ▲" : "Filter ▼");
        });

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            String filter = "ALL";
            if (checkedId == R.id.chipApproved) filter = "APPROVED";
            else if (checkedId == R.id.chipPending) filter = "PENDING";
            else if (checkedId == R.id.chipRejected) filter = "REJECTED";

            applyFilter(filter);
        });

        btnInviteMember.setOnClickListener(v -> showInviteMemberDialog());
    }

    private void loadTeamDetails() {
        String teamName = getIntent().getStringExtra("TEAM_NAME");
        String teamDescription = getIntent().getStringExtra("TEAM_DESCRIPTION");
        String createdBy = getIntent().getStringExtra("CREATED_BY");

        if (teamName != null) tvTeamName.setText(teamName);
        if (teamDescription != null) tvTeamDescription.setText(teamDescription);

        isOwner = currentUserId != null && currentUserId.equals(createdBy);
        btnInviteMember.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        // FAB chỉ hiện ở tab ReadingLists (đã control ở switchToReadingListsTab)
    }

    private void loadMembers() {
        progressBar.setVisibility(View.VISIBLE);
        teamRepository.getTeamMembers(teamId, new Callback<List<MemberResponse>>() {
            @Override
            public void onResponse(Call<List<MemberResponse>> call, Response<List<MemberResponse>> response) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<MemberResponse> members = response.body();
                    if (memberAdapter == null) {
                        memberAdapter = new MemberAdapter(members, isOwner,
                                (member, position) -> showRemoveMemberDialog(member, position));
                        rvMembers.setAdapter(memberAdapter);
                    } else {
                        memberAdapter.updateMembers(members);
                    }
                    updateMembersCount();
                    updateChipCounts();
                } else {
                    Toast.makeText(TeamDetailActivity.this, "Failed to load members", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<MemberResponse>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                ApiErrorHandler.handleNetworkFailure(TeamDetailActivity.this, t, "LoadMembers");
            }
        });
    }

    private void applyFilter(String status) {
        if (memberAdapter != null) {
            memberAdapter.applyFilter(status);
            updateMembersCount();
        }
    }

    private void updateMembersCount() {
        if (memberAdapter != null) {
            int filtered = memberAdapter.getFilteredCount();
            int total = memberAdapter.getTotalCount();
            tvMembersCount.setText(filtered == total
                    ? "Members (" + total + ")"
                    : "Members (" + filtered + " of " + total + ")");
        }
    }

    private void updateChipCounts() {
        if (memberAdapter != null) {
            int approvedCount = memberAdapter.getCountByStatus("APPROVED");
            int pendingCount = memberAdapter.getCountByStatus("PENDING");
            int rejectedCount = memberAdapter.getCountByStatus("REJECTED");
            int totalCount = memberAdapter.getTotalCount();

            chipAll.setText("All (" + totalCount + ")");
            chipApproved.setText("Active (" + approvedCount + ")");
            chipPending.setText("Pending (" + pendingCount + ")");
            chipRejected.setText("Declined (" + rejectedCount + ")");
        }
    }

    private void showRemoveMemberDialog(MemberResponse member, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Member")
                .setMessage("Are you sure you want to remove " + member.getFullName() + " from the team?")
                .setPositiveButton("Remove", (dialog, which) -> removeMember(member.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeMember(String memberId) {
        progressBar.setVisibility(View.VISIBLE);
        teamRepository.removeMember(teamId, memberId, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(TeamDetailActivity.this, "Member removed successfully", Toast.LENGTH_SHORT).show();
                    loadMembers();
                } else {
                    Toast.makeText(TeamDetailActivity.this, "Failed to remove member", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                ApiErrorHandler.handleNetworkFailure(TeamDetailActivity.this, t, "RemoveMember");
            }
        });
    }

    private void showInviteMemberDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_invite_member, null);
        builder.setView(dialogView);
        inviteDialog = builder.create();

        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSend = dialogView.findViewById(R.id.btnSend);
        ProgressBar progressBarDialog = dialogView.findViewById(R.id.progressBar);

        btnCancel.setOnClickListener(v -> { if (inviteDialog != null && inviteDialog.isShowing()) inviteDialog.dismiss(); });

        btnSend.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) { Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show(); return; }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show(); return;
            }

            progressBarDialog.setVisibility(View.VISIBLE);
            btnSend.setEnabled(false);

            InvitationRequest request = new InvitationRequest(teamId, email);
            invitationRepository.sendInvitation(request, new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (isFinishing() || isDestroyed()) return;

                    progressBarDialog.setVisibility(View.GONE);
                    btnSend.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(TeamDetailActivity.this, "Invitation sent to " + email, Toast.LENGTH_SHORT).show();
                        if (inviteDialog != null && inviteDialog.isShowing()) inviteDialog.dismiss();
                        loadMembers();
                    } else {
                        handleInviteError(response, email);
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (isFinishing() || isDestroyed()) return;

                    progressBarDialog.setVisibility(View.GONE);
                    btnSend.setEnabled(true);
                    ApiErrorHandler.handleNetworkFailure(TeamDetailActivity.this, t, "SendInvitation");
                }
            });
        });

        if (!isFinishing() && !isDestroyed()) inviteDialog.show();
    }

    private void handleInviteError(Response<ResponseBody> response, String email) {
        int statusCode = response.code();
        String errorMessage = null;

        if (statusCode == 403) {
            if (inviteDialog != null && inviteDialog.isShowing()) inviteDialog.dismiss();
            Toast.makeText(this, "You don't have permission to invite members", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                android.util.Log.d("INVITE_ERROR", "Status code: " + statusCode);
                android.util.Log.d("INVITE_ERROR", "Error body: " + errorBody);

                if (errorBody != null && errorBody.contains("\"message\"")) {
                    int startIndex = errorBody.indexOf("\"message\":\"") + 11;
                    int endIndex = errorBody.indexOf("\"", startIndex);
                    if (startIndex > 11 && endIndex > startIndex) {
                        errorMessage = errorBody.substring(startIndex, endIndex);
                    }
                }

                android.util.Log.d("INVITE_ERROR", "Parsed message: " + errorMessage);

                if (errorMessage != null &&
                        (errorMessage.toLowerCase().contains("account") && errorMessage.toLowerCase().contains("not found"))) {
                    Toast.makeText(this, "User with email " + email + " not found in the system", Toast.LENGTH_LONG).show();
                    if (inviteDialog != null && inviteDialog.isShowing()) inviteDialog.dismiss();
                    return;
                }

                if (errorMessage != null &&
                        (errorMessage.toLowerCase().contains("already a member")
                                || errorMessage.toLowerCase().contains("already member")
                                || errorMessage.toLowerCase().contains("is already a member"))) {
                    Toast.makeText(this, email + " is already a member of this team", Toast.LENGTH_LONG).show();
                    if (inviteDialog != null && inviteDialog.isShowing()) inviteDialog.dismiss();
                    return;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("INVITE_ERROR", "Parse error", e);
        }

        if (errorMessage == null || errorMessage.isEmpty()) errorMessage = "Failed to send invitation";
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        if (inviteDialog != null && inviteDialog.isShowing()) inviteDialog.dismiss();
    }

    private void switchToMembersTab() {
        layoutMembers.setVisibility(View.VISIBLE);
        layoutReadingLists.setVisibility(View.GONE);

        tabMembers.setTextColor(getColor(android.R.color.holo_blue_dark));
        tabMembers.setTypeface(null, Typeface.BOLD);
        tabMembers.setBackgroundResource(R.drawable.tab_selected);

        tabReadingLists.setTextColor(0xFF61758A);
        tabReadingLists.setTypeface(null, Typeface.NORMAL);
        tabReadingLists.setBackground(null);
        fabCreateReadingList.setVisibility(View.GONE);
    }

    private void switchToReadingListsTab() {
        layoutMembers.setVisibility(View.GONE);
        layoutReadingLists.setVisibility(View.VISIBLE);

        tabReadingLists.setTextColor(getColor(android.R.color.holo_blue_dark));
        tabReadingLists.setTypeface(null, Typeface.BOLD);
        tabReadingLists.setBackgroundResource(R.drawable.tab_selected);

        tabMembers.setTextColor(0xFF61758A);
        tabMembers.setTypeface(null, Typeface.NORMAL);
        tabMembers.setBackground(null);

        fabCreateReadingList.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        loadReadingLists();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isOwner) getMenuInflater().inflate(R.menu.menu_team_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_team) { editTeam(); return true; }
        else if (id == R.id.action_delete_team) { showDeleteTeamDialog(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void editTeam() {
        Intent intent = new Intent(this, EditTeamActivity.class);
        intent.putExtra("TEAM_ID", teamId);
        intent.putExtra("TEAM_NAME", tvTeamName.getText().toString());
        intent.putExtra("TEAM_DESCRIPTION", tvTeamDescription.getText().toString());
        editTeamLauncher.launch(intent);
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
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(TeamDetailActivity.this, "Team deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(TeamDetailActivity.this, "Failed to delete team", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                ApiErrorHandler.handleNetworkFailure(TeamDetailActivity.this, t, "DeleteTeam");
            }
        });
    }

    private void loadReadingLists() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyReadingLists.setVisibility(View.GONE);

        teamRepository.getReadingLists(teamId, new Callback<List<TeamReadingListResponse>>() {
            @Override
            public void onResponse(Call<List<TeamReadingListResponse>> call, Response<List<TeamReadingListResponse>> response) {
                if (isFinishing() || isDestroyed()) return;

                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<TeamReadingListResponse> readingLists = response.body();

                    if (readingLists.isEmpty()) {
                        tvEmptyReadingLists.setVisibility(View.VISIBLE);
                        rvReadingLists.setVisibility(View.GONE);
                    } else {
                        tvEmptyReadingLists.setVisibility(View.GONE);
                        rvReadingLists.setVisibility(View.VISIBLE);

                        if (teamReadingListsAdapter == null) {
                            teamReadingListsAdapter = new TeamReadingListAdapter(
                                    readingLists,
                                    isOwner,
                                    new TeamReadingListAdapter.OnAction() {
                                        @Override
                                        public void onItemClick(TeamReadingListResponse readingList) {
                                            viewReadingListDetail(readingList);
                                        }
                                        @Override
                                        public void onEditClick(TeamReadingListResponse readingList, int position) {
                                            showEditReadingListDialog(readingList);
                                        }
                                        @Override
                                        public void onDeleteClick(TeamReadingListResponse readingList, int position) {
                                            showDeleteReadingListDialog(readingList);
                                        }
                                    }
                            );
                            rvReadingLists.setAdapter(teamReadingListsAdapter);
                        } else {
                            // dùng API mới của adapter
                            teamReadingListsAdapter.submit(readingLists);
                        }
                    }
                } else {
                    tvEmptyReadingLists.setVisibility(View.VISIBLE);
                    Toast.makeText(TeamDetailActivity.this, "Failed to load reading lists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TeamReadingListResponse>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;

                progressBar.setVisibility(View.GONE);
                tvEmptyReadingLists.setVisibility(View.VISIBLE);
                ApiErrorHandler.handleNetworkFailure(TeamDetailActivity.this, t, "LoadReadingLists");
            }
        });
    }

    private void showCreateReadingListDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reading_list, null);
        builder.setView(dialogView);
        readingListDialog = builder.create();

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        ProgressBar progressBarDialog = dialogView.findViewById(R.id.progressBar);

        tvDialogTitle.setText("Create Reading List");
        btnSave.setText("Create");

        btnCancel.setOnClickListener(v -> { if (readingListDialog != null && readingListDialog.isShowing()) readingListDialog.dismiss(); });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            if (name.isEmpty()) { Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show(); return; }

            progressBarDialog.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);

            TeamReadingListRequest request = new TeamReadingListRequest(name, description);
            teamRepository.createReadingList(teamId, request, new Callback<TeamReadingListResponse>() {
                @Override
                public void onResponse(Call<TeamReadingListResponse> call, Response<TeamReadingListResponse> response) {
                    if (isFinishing() || isDestroyed()) return;

                    progressBarDialog.setVisibility(View.GONE);
                    btnSave.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(TeamDetailActivity.this, "Reading list created", Toast.LENGTH_SHORT).show();
                        if (readingListDialog != null && readingListDialog.isShowing()) readingListDialog.dismiss();
                        loadReadingLists();
                    } else {
                        Toast.makeText(TeamDetailActivity.this, "Failed to create reading list", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<TeamReadingListResponse> call, Throwable t) {
                    if (isFinishing() || isDestroyed()) return;

                    progressBarDialog.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    ApiErrorHandler.handleNetworkFailure(TeamDetailActivity.this, t, "CreateReadingList");
                }
            });
        });

        if (!isFinishing() && !isDestroyed()) readingListDialog.show();
    }

    private void showEditReadingListDialog(TeamReadingListResponse readingList) {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reading_list, null);
        builder.setView(dialogView);
        readingListDialog = builder.create();

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        ProgressBar progressBarDialog = dialogView.findViewById(R.id.progressBar);

        tvDialogTitle.setText("Edit Reading List");
        btnSave.setText("Update");
        etName.setText(readingList.getName());
        etDescription.setText(readingList.getDescription());

        btnCancel.setOnClickListener(v -> { if (readingListDialog != null && readingListDialog.isShowing()) readingListDialog.dismiss(); });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            if (name.isEmpty()) { Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show(); return; }

            progressBarDialog.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);

            TeamReadingListRequest request = new TeamReadingListRequest(name, description);
            teamRepository.updateReadingList(teamId, readingList.getId(), request, new Callback<TeamReadingListResponse>() {
                @Override
                public void onResponse(Call<TeamReadingListResponse> call, Response<TeamReadingListResponse> response) {
                    if (isFinishing() || isDestroyed()) return;

                    progressBarDialog.setVisibility(View.GONE);
                    btnSave.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(TeamDetailActivity.this, "Reading list updated", Toast.LENGTH_SHORT).show();
                        if (readingListDialog != null && readingListDialog.isShowing()) readingListDialog.dismiss();
                        loadReadingLists();
                    } else {
                        Toast.makeText(TeamDetailActivity.this, "Failed to update reading list", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<TeamReadingListResponse> call, Throwable t) {
                    if (isFinishing() || isDestroyed()) return;

                    progressBarDialog.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    ApiErrorHandler.handleNetworkFailure(TeamDetailActivity.this, t, "UpdateReadingList");
                }
            });
        });

        if (!isFinishing() && !isDestroyed()) readingListDialog.show();
    }

    private void showDeleteReadingListDialog(TeamReadingListResponse readingList) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Reading List")
                .setMessage("Are you sure you want to delete \"" + readingList.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteReadingList(readingList.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteReadingList(String readingListId) {
        progressBar.setVisibility(View.VISIBLE);
        teamRepository.deleteReadingList(teamId, readingListId, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (isFinishing() || isDestroyed()) return;

                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(TeamDetailActivity.this, "Reading list deleted", Toast.LENGTH_SHORT).show();
                    loadReadingLists();
                } else {
                    Toast.makeText(TeamDetailActivity.this, "Failed to delete reading list", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;

                progressBar.setVisibility(View.GONE);
                ApiErrorHandler.handleNetworkFailure(TeamDetailActivity.this, t, "DeleteReadingList");
            }
        });
    }

    private void viewReadingListDetail(TeamReadingListResponse readingList) {
        Toast.makeText(this, "View: " + readingList.getName(), Toast.LENGTH_SHORT).show();
        // TODO: start Activity hiển thị chi tiết reading list
    }

    @Override
    protected void onDestroy() {
        if (inviteDialog != null && inviteDialog.isShowing()) inviteDialog.dismiss();
        if (readingListDialog != null && readingListDialog.isShowing()) readingListDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
