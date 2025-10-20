package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.team.TeamResponse;

import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private List<TeamResponse> teams;
    private OnTeamClickListener listener;

    public interface OnTeamClickListener {
        void onTeamClick(TeamResponse team);
    }

    public TeamAdapter(List<TeamResponse> teams, OnTeamClickListener listener) {
        this.teams = teams;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        TeamResponse team = teams.get(position);
        holder.tvName.setText(team.getName());
        holder.tvDescription.setText(team.getDescription());

        holder.itemView.setOnClickListener(v -> listener.onTeamClick(team));
    }

    @Override
    public int getItemCount() {
        return teams != null ? teams.size() : 0;
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTeamName);
            tvDescription = itemView.findViewById(R.id.tvTeamDescription);
        }
    }
}
