package com.prm392.g5.labverse.activity.team;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.team.TeamRequest;
import com.prm392.g5.labverse.dto.team.TeamResponse;
import com.prm392.g5.labverse.repository.TeamRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTeamActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout tilTeamName, tilTeamDescription;
    private TextInputEditText etTeamName, etTeamDescription;
    private Button btnCreateTeam;
    private ProgressBar progressBar;

    private TeamRepository teamRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);

        initViews();
        setupToolbar();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilTeamName = findViewById(R.id.tilTeamName);
        tilTeamDescription = findViewById(R.id.tilTeamDescription);
        etTeamName = findViewById(R.id.etTeamName);
        etTeamDescription = findViewById(R.id.etTeamDescription);
        btnCreateTeam = findViewById(R.id.btnCreateTeam);
        progressBar = findViewById(R.id.progressBar);

        teamRepository = new TeamRepository();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnCreateTeam.setOnClickListener(v -> createTeam());

        // Clear errors when user starts typing
        etTeamName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tilTeamName.setError(null);
            }
        });

        etTeamDescription.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tilTeamDescription.setError(null);
            }
        });
    }

    private void createTeam() {
        String name = etTeamName.getText().toString().trim();
        String description = etTeamDescription.getText().toString().trim();

        // Validation
        if (!validateInput(name, description)) {
            return;
        }

        // Show loading state
        setLoadingState(true);

        // Create team request
        TeamRequest request = new TeamRequest();
        request.setName(name);
        request.setDescription(description);

        teamRepository.createTeam(request, new Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    TeamResponse team = response.body();
                    Toast.makeText(CreateTeamActivity.this,
                            "Team '" + team.getName() + "' created successfully!",
                            Toast.LENGTH_SHORT).show();

                    // Return to previous screen with result
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMessage = "Failed to create team";

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();

                            // Parse error message
                            if (errorBody.contains("\"message\"")) {
                                int startIndex = errorBody.indexOf("\"message\":\"") + 11;
                                int endIndex = errorBody.indexOf("\"", startIndex);
                                if (startIndex > 11 && endIndex > startIndex) {
                                    errorMessage = errorBody.substring(startIndex, endIndex);
                                }
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("CREATE_TEAM", "Parse error", e);
                    }

                    Toast.makeText(CreateTeamActivity.this,
                            errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                setLoadingState(false);
                Toast.makeText(CreateTeamActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInput(String name, String description) {
        boolean isValid = true;

        // Validate team name
        if (name.isEmpty()) {
            tilTeamName.setError("Team name is required");
            etTeamName.requestFocus();
            isValid = false;
        } else if (name.length() < 3) {
            tilTeamName.setError("Team name must be at least 3 characters");
            etTeamName.requestFocus();
            isValid = false;
        } else if (name.length() > 100) {
            tilTeamName.setError("Team name is too long");
            etTeamName.requestFocus();
            isValid = false;
        } else {
            tilTeamName.setError(null);
        }

        // Validate description (optional but has limits if provided)
        if (description.length() > 500) {
            tilTeamDescription.setError("Description is too long (max 500 characters)");
            if (isValid) {
                etTeamDescription.requestFocus();
            }
            isValid = false;
        } else {
            tilTeamDescription.setError(null);
        }

        return isValid;
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnCreateTeam.setEnabled(false);
            btnCreateTeam.setText("Creating...");
            etTeamName.setEnabled(false);
            etTeamDescription.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnCreateTeam.setEnabled(true);
            btnCreateTeam.setText("Create Team");
            etTeamName.setEnabled(true);
            etTeamDescription.setEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}