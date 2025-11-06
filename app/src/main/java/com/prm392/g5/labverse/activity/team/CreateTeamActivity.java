package com.prm392.g5.labverse.activity.team;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.team.TeamRequest;
import com.prm392.g5.labverse.dto.team.TeamResponse;
import com.prm392.g5.labverse.repository.TeamRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTeamActivity extends AppCompatActivity {

    private EditText etTeamName, etTeamDescription;
    private Button btnCreateTeam;
    private ProgressBar progressBar;

    private TeamRepository teamRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);

        etTeamName = findViewById(R.id.etTeamName);
        etTeamDescription = findViewById(R.id.etTeamDescription);
        btnCreateTeam = findViewById(R.id.btnCreateTeam);
        progressBar = findViewById(R.id.progressBar);

        teamRepository = new TeamRepository();

        btnCreateTeam.setOnClickListener(v -> createTeam());
    }

    private void createTeam() {
        String name = etTeamName.getText().toString().trim();
        String description = etTeamDescription.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Team name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCreateTeam.setEnabled(false);

        TeamRequest request = new TeamRequest();
        request.setName(name);
        request.setDescription(description);

        teamRepository.createTeam(request, new Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnCreateTeam.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CreateTeamActivity.this, "Team created successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại màn hình trước (list team)
                } else {
                    Toast.makeText(CreateTeamActivity.this, "Failed to create team", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCreateTeam.setEnabled(true);
                Toast.makeText(CreateTeamActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
