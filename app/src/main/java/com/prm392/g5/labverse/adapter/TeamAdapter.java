package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.team.TeamResponse;

import java.util.List;
import java.util.function.Consumer;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private final List<TeamResponse> teamList;
    private final Consumer<TeamResponse> onClick;

    public TeamAdapter(List<TeamResponse> teamList, Consumer<TeamResponse> onClick) {
        this.teamList = teamList;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        TeamResponse team = teamList.get(position);

        holder.tvTeamName.setText(team.getName() != null ? team.getName() : "Unnamed Team");
        holder.tvTeamDescription.setText(team.getDescription() != null ? team.getDescription() : "No description");

        holder.itemView.setOnClickListener(v -> onClick.accept(team));
    }


    @Override
    public int getItemCount() {
        return teamList.size();
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeamName, tvTeamDescription, tvTeamInfo;
        Button btnEdit;
        ImageView imgTeam;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvTeamDescription = itemView.findViewById(R.id.tvTeamDescription);
        }
    }

}
