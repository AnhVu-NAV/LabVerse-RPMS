package com.prm392.g5.labverse.activity.team;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.TeamAdapter;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dto.team.TeamResponse;
import com.prm392.g5.labverse.repository.TeamRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListMyTeamsActivity extends AppCompatActivity {

    private RecyclerView rvTeams;
    private ProgressBar progressBar;
    private TextView tvEmptyTeams;
    private Button btnAdd;   // Nếu user không được tạo team thì có thể ẩn button này

    private TeamAdapter adapter;
    private TeamRepository teamRepository;
    private final List<TeamResponse> teamList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_team_of_pi);

        rvTeams = findViewById(R.id.rvTeams);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyTeams = findViewById(R.id.tvEmptyTeams);
        btnAdd = findViewById(R.id.btnAdd);

        btnAdd.setVisibility(View.GONE);

        rvTeams.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TeamAdapter(teamList, team -> {
            if (team == null) {
                Toast.makeText(this, "Team data is null!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (team.getId() == null || team.getId().isEmpty()) {
                Toast.makeText(this, "Team ID is missing!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(ListMyTeamsActivity.this, TeamDetailActivity.class);
            intent.putExtra("TEAM_ID", team.getId());
            intent.putExtra("TEAM_NAME", team.getName());
            intent.putExtra("TEAM_DESCRIPTION", team.getDescription());

            // Ở đây phải truyền đúng CREATED_BY = owner của team (từ response),
            // để TeamDetailActivity tự tính isOwner = currentUserId.equals(createdBy)
            intent.putExtra("CREATED_BY", team.getCreatedBy());

            startActivity(intent);
        });

        rvTeams.setAdapter(adapter);

        teamRepository = new TeamRepository();
        loadTeams();
    }

    private void loadTeams() {
        progressBar.setVisibility(View.VISIBLE);
        rvTeams.setVisibility(View.GONE);
        tvEmptyTeams.setVisibility(View.GONE);

        // GỌI API GÓC NHÌN USER
        teamRepository.getMyTeams(new Callback<List<TeamResponse>>() {
            @Override
            public void onResponse(Call<List<TeamResponse>> call, Response<List<TeamResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    teamList.clear();
                    teamList.addAll(response.body());

                    if (teamList.isEmpty()) {
                        tvEmptyTeams.setVisibility(View.VISIBLE);
                        tvEmptyTeams.setText("You are not in any team yet");
                    } else {
                        rvTeams.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(ListMyTeamsActivity.this,
                            "Failed to load teams", Toast.LENGTH_SHORT).show();
                    tvEmptyTeams.setVisibility(View.VISIBLE);
                    tvEmptyTeams.setText("Failed to load teams");
                }
            }

            @Override
            public void onFailure(Call<List<TeamResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmptyTeams.setVisibility(View.VISIBLE);
                tvEmptyTeams.setText("Error: " + t.getMessage());
                Toast.makeText(ListMyTeamsActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeams();
    }
}
