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
import com.prm392.g5.labverse.adapter.MyPaperAdapter;
import com.prm392.g5.labverse.dto.paper.PaperInfoResponse;
import com.prm392.g5.labverse.dto.team.TeamReadingListPaperResponse;
import com.prm392.g5.labverse.repository.PaperRepository;
import com.prm392.g5.labverse.repository.TeamRepository;
import com.prm392.g5.labverse.util.ApiErrorHandler;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectMyPaperActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvPapers;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private MyPaperAdapter adapter;
    private PaperRepository paperRepository;
    private TeamRepository teamRepository;

    private String teamId;
    private String readingListId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_my_paper);

        initViews();
        loadIntentData();
        setupToolbar();
        loadMyPapers();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvPapers = findViewById(R.id.rvPapers);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);

        rvPapers.setLayoutManager(new LinearLayoutManager(this));

        paperRepository = new PaperRepository();
        teamRepository = new TeamRepository();
    }

    private void loadIntentData() {
        teamId = getIntent().getStringExtra("TEAM_ID");
        readingListId = getIntent().getStringExtra("READING_LIST_ID");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Paper");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadMyPapers() {
        progressBar.setVisibility(View.VISIBLE);
        rvPapers.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        paperRepository.getMyPapers(new Callback<List<PaperInfoResponse>>() {
            @Override
            public void onResponse(Call<List<PaperInfoResponse>> call, Response<List<PaperInfoResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<PaperInfoResponse> papers = response.body();

                    if (papers.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvPapers.setVisibility(View.VISIBLE);
                        if (adapter == null) {
                            adapter = new MyPaperAdapter(papers, paper -> {
                                // Khi chọn paper -> call API add
                                addPaperToReadingList(paper);
                            });
                            rvPapers.setAdapter(adapter);
                        } else {
                            adapter.updatePapers(papers);
                        }
                    }
                } else {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    ApiErrorHandler.handleApiResponseError(
                            SelectMyPaperActivity.this,
                            response,
                            "LoadMyPapers"
                    );
                }
            }

            @Override
            public void onFailure(Call<List<PaperInfoResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                ApiErrorHandler.handleNetworkFailure(
                        SelectMyPaperActivity.this,
                        t,
                        "LoadMyPapers"
                );
            }
        });
    }

    private void addPaperToReadingList(PaperInfoResponse paper) {
        if (teamId == null || readingListId == null || paper.getId() == null) {
            Toast.makeText(this, "Missing required info!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        teamRepository.addPaperToReadingList(
                teamId,
                readingListId,
                paper.getId(),
                new Callback<TeamReadingListPaperResponse>() {
                    @Override
                    public void onResponse(Call<TeamReadingListPaperResponse> call, Response<TeamReadingListPaperResponse> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(SelectMyPaperActivity.this,
                                    "Added: " + paper.getTitle(),
                                    Toast.LENGTH_SHORT).show();

                            setResult(RESULT_OK);
                            finish();
                        } else {
                            ApiErrorHandler.handleApiResponseError(
                                    SelectMyPaperActivity.this,
                                    response,
                                    "AddPaperToReadingList"
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<TeamReadingListPaperResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        ApiErrorHandler.handleNetworkFailure(
                                SelectMyPaperActivity.this,
                                t,
                                "AddPaperToReadingList"
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
