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
import com.prm392.g5.labverse.util.ApiErrorHandler;

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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Team");
        }

        toolbar.setNavigationOnClickListener(v -> finish());

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

        //  Validation
        if (name.isEmpty()) {
            etTeamName.setError("Team name is required");
            etTeamName.requestFocus();
            return;
        }

        if (name.length() < 3) {
            etTeamName.setError("Team name must be at least 3 characters");
            etTeamName.requestFocus();
            return;
        }

        if (name.length() > 100) {
            etTeamName.setError("Team name is too long");
            etTeamName.requestFocus();
            return;
        }

        if (description.length() > 500) {
            etTeamDescription.setError("Description is too long (max 500 characters)");
            etTeamDescription.requestFocus();
            return;
        }

        //  Set loading state
        progressBar.setVisibility(View.VISIBLE);
        btnSaveTeam.setEnabled(false);
        btnSaveTeam.setText("Updating...");
        etTeamName.setEnabled(false);
        etTeamDescription.setEnabled(false);

        TeamRequest request = new TeamRequest();
        request.setName(name);
        request.setDescription(description);


        teamRepository.updateTeam(teamId, request, new Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnSaveTeam.setEnabled(true);
                btnSaveTeam.setText("Save Changes");
                etTeamName.setEnabled(true);
                etTeamDescription.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    TeamResponse updatedTeam = response.body();

                    Toast.makeText(EditTeamActivity.this,
                            " Team updated successfully!", Toast.LENGTH_SHORT).show();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("UPDATED_TEAM_NAME", updatedTeam.getName());
                    resultIntent.putExtra("UPDATED_TEAM_DESCRIPTION", updatedTeam.getDescription());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    ApiErrorHandler.handleApiResponseError(EditTeamActivity.this, response, "EditTeam");
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSaveTeam.setEnabled(true);
                btnSaveTeam.setText("Save Changes");
                etTeamName.setEnabled(true);
                etTeamDescription.setEnabled(true);
                Toast.makeText(EditTeamActivity.this, "Failed to update team", Toast.LENGTH_SHORT).show();
                ApiErrorHandler.handleNetworkFailure(EditTeamActivity.this, t, "EditTeam");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


}