package com.prm392.g5.labverse.activity.team;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.team.TeamRequest;
import com.prm392.g5.labverse.dto.team.TeamResponse;
import com.prm392.g5.labverse.repository.TeamRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTeamActivity extends AppCompatActivity {

    private EditText etTeamName, etTeamDescription;
    private Button btnSaveTeam;
    private ProgressBar progressBar;

    private TeamRepository teamRepository;
    private String teamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_team);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Team");

        etTeamName = findViewById(R.id.etTeamName);
        etTeamDescription = findViewById(R.id.etTeamDescription);
        btnSaveTeam = findViewById(R.id.btnSaveTeam);
        progressBar = findViewById(R.id.progressBar);

        teamRepository = new TeamRepository();

        // Get data from intent
        teamId = getIntent().getStringExtra("TEAM_ID");
        String teamName = getIntent().getStringExtra("TEAM_NAME");
        String teamDescription = getIntent().getStringExtra("TEAM_DESCRIPTION");

        if (teamId == null) {
            Toast.makeText(this, "Missing team ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Pre-fill data
        etTeamName.setText(teamName);
        etTeamDescription.setText(teamDescription);

        btnSaveTeam.setOnClickListener(v -> updateTeam());
    }

    private void updateTeam() {
        String name = etTeamName.getText().toString().trim();
        String description = etTeamDescription.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Team name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSaveTeam.setEnabled(false);

        TeamRequest request = new TeamRequest();
        request.setName(name);
        request.setDescription(description);

        teamRepository.updateTeam(teamId, request, new Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnSaveTeam.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditTeamActivity.this,
                            "Team updated successfully!", Toast.LENGTH_SHORT).show();

                    // Return updated data to parent activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("TEAM_NAME", name);
                    resultIntent.putExtra("TEAM_DESCRIPTION", description);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(EditTeamActivity.this,
                            "Failed to update team", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSaveTeam.setEnabled(true);
                Toast.makeText(EditTeamActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}