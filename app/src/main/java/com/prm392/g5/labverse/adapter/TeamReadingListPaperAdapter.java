package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.team.TeamReadingListPaperResponse;

import java.util.ArrayList;
import java.util.List;

public class TeamReadingListPaperAdapter extends RecyclerView.Adapter<TeamReadingListPaperAdapter.ViewHolder> {

    private List<TeamReadingListPaperResponse> papers;
    private boolean isOwner;
    private OnPaperActionListener listener;

    public interface OnPaperActionListener {
        void onSetPriorityClick(TeamReadingListPaperResponse paper, int position);
    }

    public TeamReadingListPaperAdapter(List<TeamReadingListPaperResponse> papers, boolean isOwner, OnPaperActionListener listener) {
        this.papers = papers != null ? papers : new ArrayList<>();
        this.isOwner = isOwner;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reading_list_paper, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeamReadingListPaperResponse paper = papers.get(position);

        // Hiển thị title và authorName từ TeamReadingListPaper
        holder.tvPaperTitle.setText(paper.getTitle() != null ? paper.getTitle() : "Title not available");
        holder.tvPaperAuthors.setText("Author: " + (paper.getAuthorName() != null ? paper.getAuthorName() : "Author not available"));

        // Priority badge
        String priority = paper.getPriority() != null ? paper.getPriority() : "MEDIUM";
        holder.chipPriority.setText(priority);

        switch (priority.toUpperCase()) {
            case "HIGH":
                holder.chipPriority.setChipBackgroundColorResource(android.R.color.holo_red_dark);
                break;
            case "MEDIUM":
                holder.chipPriority.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
                break;
            case "LOW":
                holder.chipPriority.setChipBackgroundColorResource(android.R.color.holo_green_dark);
                break;
            default:
                holder.chipPriority.setChipBackgroundColorResource(android.R.color.darker_gray);
        }

        if (isOwner) {
            holder.btnSetPriority.setVisibility(View.VISIBLE);
            holder.btnSetPriority.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSetPriorityClick(paper, position);
                }
            });
        } else {
            holder.btnSetPriority.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return papers.size();
    }

    public void updatePapers(List<TeamReadingListPaperResponse> newPapers) {
        this.papers = newPapers != null ? newPapers : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPaperTitle, tvPaperAuthors, tvPaperYear;
        Chip chipPriority;
        Button btnSetPriority;

        ViewHolder(View itemView) {
            super(itemView);
            tvPaperTitle = itemView.findViewById(R.id.tvPaperTitle);
            tvPaperAuthors = itemView.findViewById(R.id.tvPaperAuthors);
            chipPriority = itemView.findViewById(R.id.chipPriority);
            btnSetPriority = itemView.findViewById(R.id.btnSetPriority);
        }
    }
}
