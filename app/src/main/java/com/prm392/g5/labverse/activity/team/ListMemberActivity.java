package com.prm392.g5.labverse.activity.team;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.MemberAdapter;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dto.team.MemberResponse;
import com.prm392.g5.labverse.repository.TeamRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListMemberActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MemberAdapter adapter;
    private TeamRepository teamRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_member);

        recyclerView = findViewById(R.id.recyclerMembers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        teamRepository = new TeamRepository();

        String teamId = getIntent().getStringExtra("TEAM_ID");
        if (teamId == null) {
            Toast.makeText(this, "Missing team ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMembers(teamId);
    }

    private void loadMembers(String teamId) {
        teamRepository.getTeamMembers(teamId, new Callback<List<MemberResponse>>() {
            @Override
            public void onResponse(Call<List<MemberResponse>> call, Response<List<MemberResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MemberResponse> members = response.body();

                    String createdBy = getIntent().getStringExtra("CREATED_BY");
                    String currentUserId = SharePreferenceManager.getInstance().getUserId();
                    boolean isOwner = currentUserId != null && currentUserId.equals(createdBy);

                    adapter = new MemberAdapter(members, isOwner);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(ListMemberActivity.this, "Failed to load members", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MemberResponse>> call, Throwable t) {
                Toast.makeText(ListMemberActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
