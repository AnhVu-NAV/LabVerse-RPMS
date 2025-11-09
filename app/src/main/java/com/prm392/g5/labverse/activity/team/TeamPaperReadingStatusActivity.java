package com.prm392.g5.labverse.activity.team;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.MemberReadingStatusAdapter;
import com.prm392.g5.labverse.dto.team.TeamReadingStatusResponse;
import com.prm392.g5.labverse.repository.ReadingStatusRepository;
import com.prm392.g5.labverse.util.ApiErrorHandler;

import java.util.List;

import retrofit2.Call;

import retrofit2.Callback;
import retrofit2.Response;

public class TeamPaperReadingStatusActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvMemberStatus;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private MemberReadingStatusAdapter adapter;
    private ReadingStatusRepository readingStatusRepository;

    private String teamId;
    private String paperId;
    private String paperTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_paper_reading_status);

        initViews();
        loadIntentData();
        setupToolbar();
        loadTeamReadingStatus();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMemberStatus = findViewById(R.id.rvMemberStatus);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);

        rvMemberStatus.setLayoutManager(new LinearLayoutManager(this));
        readingStatusRepository = new ReadingStatusRepository();
    }

    private void loadIntentData() {
        teamId = getIntent().getStringExtra("TEAM_ID");
        paperId = getIntent().getStringExtra("PAPER_ID");
        paperTitle = getIntent().getStringExtra("PAPER_TITLE");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(paperTitle != null ? paperTitle : "Reading Progress");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadTeamReadingStatus() {
        progressBar.setVisibility(View.VISIBLE);
        rvMemberStatus.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        readingStatusRepository.getTeamReadingStatus(teamId, paperId,
                new Callback<List<TeamReadingStatusResponse>>() {
                    @Override
                    public void onResponse(Call<List<TeamReadingStatusResponse>> call,
                                           Response<List<TeamReadingStatusResponse>> response) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            List<TeamReadingStatusResponse> statusList = response.body();

                            if (statusList.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                Toast.makeText(TeamPaperReadingStatusActivity.this,
                                        "No reading progress found",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                rvMemberStatus.setVisibility(View.VISIBLE);

                                if (adapter == null) {
                                    adapter = new MemberReadingStatusAdapter(statusList);
                                    rvMemberStatus.setAdapter(adapter);
                                } else {
                                    adapter.updateStatuses(statusList);
                                }
                            }
                        } else {
                            layoutEmpty.setVisibility(View.VISIBLE);
                            ApiErrorHandler.handleApiResponseError(
                                    TeamPaperReadingStatusActivity.this,
                                    response,
                                    "LoadReadingStatus"
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TeamReadingStatusResponse>> call, Throwable t) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        progressBar.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                        ApiErrorHandler.handleNetworkFailure(
                                TeamPaperReadingStatusActivity.this,
                                t,
                                "LoadReadingStatus"
                        );
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}