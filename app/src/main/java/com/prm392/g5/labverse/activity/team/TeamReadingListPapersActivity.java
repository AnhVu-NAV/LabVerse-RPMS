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
import com.prm392.g5.labverse.adapter.TeamReadingListPaperAdapter;
import com.prm392.g5.labverse.dto.team.SetPaperPriorityRequest;
import com.prm392.g5.labverse.dto.team.TeamReadingListPaperResponse;
import com.prm392.g5.labverse.repository.TeamRepository;
import com.prm392.g5.labverse.util.ApiErrorHandler;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamReadingListPapersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvPapers;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private TeamReadingListPaperAdapter adapter; // ✅ Updated type
    private TeamRepository teamRepository;

    private String teamId;
    private String readingListId;
    private String readingListName;
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_reading_list_papers);

        initViews();
        loadIntentData();
        setupToolbar();
        loadPapers();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvPapers = findViewById(R.id.rvPapers);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);

        rvPapers.setLayoutManager(new LinearLayoutManager(this));
        teamRepository = new TeamRepository();
    }

    private void loadIntentData() {
        teamId = getIntent().getStringExtra("TEAM_ID");
        readingListId = getIntent().getStringExtra("READING_LIST_ID");
        readingListName = getIntent().getStringExtra("READING_LIST_NAME");
        isOwner = getIntent().getBooleanExtra("IS_OWNER", false);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(readingListName != null ? readingListName : "Papers");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadPapers() {
        progressBar.setVisibility(View.VISIBLE);
        rvPapers.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        teamRepository.getReadingListPapers(teamId, readingListId, new Callback<List<TeamReadingListPaperResponse>>() {
            @Override
            public void onResponse(Call<List<TeamReadingListPaperResponse>> call, Response<List<TeamReadingListPaperResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<TeamReadingListPaperResponse> papers = response.body();

                    if (papers.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvPapers.setVisibility(View.VISIBLE);

                        if (adapter == null) {
                            //  Updated adapter instantiation
                            adapter = new TeamReadingListPaperAdapter(papers, isOwner,
                                    new TeamReadingListPaperAdapter.OnPaperActionListener() {
                                        @Override
                                        public void onSetPriorityClick(TeamReadingListPaperResponse paper, int position) {
                                            showSetPriorityDialog(paper, position);
                                        }
                                    });
                            rvPapers.setAdapter(adapter);
                        } else {
                            adapter.updatePapers(papers);
                        }
                    }
                } else {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    ApiErrorHandler.handleApiResponseError(
                            TeamReadingListPapersActivity.this,
                            response,
                            "LoadPapers"
                    );
                }
            }

            @Override
            public void onFailure(Call<List<TeamReadingListPaperResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                ApiErrorHandler.handleNetworkFailure(
                        TeamReadingListPapersActivity.this,
                        t,
                        "LoadPapers"
                );
            }
        });
    }

    private void showSetPriorityDialog(TeamReadingListPaperResponse paper, int position) {
        String currentPriority = paper.getPriority() != null ? paper.getPriority() : "MEDIUM";

        String[] priorities = {"HIGH", "MEDIUM", "LOW"};
        int checkedItem = 1; // Default MEDIUM

        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i].equalsIgnoreCase(currentPriority)) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Set Paper Priority")
                .setSingleChoiceItems(priorities, checkedItem, null)
                .setPositiveButton("Save", (dialog, which) -> {
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String selectedPriority = priorities[selectedPosition];
                    updatePaperPriority(paper, selectedPriority);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updatePaperPriority(TeamReadingListPaperResponse paper, String priority) {
        progressBar.setVisibility(View.VISIBLE);

        SetPaperPriorityRequest request = new SetPaperPriorityRequest(priority);

        teamRepository.updatePaperPriority(
                teamId,
                readingListId,
                paper.getPaperId(),
                request,
                new Callback<TeamReadingListPaperResponse>() {
                    @Override
                    public void onResponse(Call<TeamReadingListPaperResponse> call, Response<TeamReadingListPaperResponse> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful()) {
                            Toast.makeText(TeamReadingListPapersActivity.this,
                                    "Priority updated to " + priority,
                                    Toast.LENGTH_SHORT).show();
                            loadPapers(); // Reload to reflect changes
                        } else {
                            ApiErrorHandler.handleApiResponseError(
                                    TeamReadingListPapersActivity.this,
                                    response,
                                    "UpdatePriority"
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<TeamReadingListPaperResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        ApiErrorHandler.handleNetworkFailure(
                                TeamReadingListPapersActivity.this,
                                t,
                                "UpdatePriority"
                        );
                    }
                }
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}