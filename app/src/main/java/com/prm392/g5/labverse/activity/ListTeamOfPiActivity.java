package com.prm392.g5.labverse.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.TeamAdapter;
import com.prm392.g5.labverse.dto.team.TeamResponse;
import com.prm392.g5.labverse.repository.TeamRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListTeamOfPiActivity extends AppCompatActivity {

    private RecyclerView rvTeams;
    private ProgressBar progressBar;
    private TeamAdapter adapter;
    private TeamRepository teamRepository;
    private List<TeamResponse> teamList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_team_of_pi);

        rvTeams = findViewById(R.id.rvTeams);
        progressBar = findViewById(R.id.progressBar);

        rvTeams.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeamAdapter(teamList, team -> {
            Toast.makeText(this, "Clicked: " + team.getName(), Toast.LENGTH_SHORT).show();
            // TODO: chuyển sang Activity chi tiết team
        });
        rvTeams.setAdapter(adapter);

        teamRepository = new TeamRepository();

        loadTeams();
    }

    private void loadTeams() {
        progressBar.setVisibility(View.VISIBLE);

        teamRepository.getListTeamOfPi(new Callback<List<TeamResponse>>() {
            @Override
            public void onResponse(Call<List<TeamResponse>> call, Response<List<TeamResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    teamList.clear();
                    teamList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ListTeamOfPiActivity.this, "Failed to load teams", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TeamResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ListTeamOfPiActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
