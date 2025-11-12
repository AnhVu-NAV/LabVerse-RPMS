package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.team.TeamReadingListPaperResponse;

import java.util.List;

public class TeamReadingListPaperAdapter extends RecyclerView.Adapter<TeamReadingListPaperAdapter.PaperViewHolder> {

    private List<TeamReadingListPaperResponse> papers;
    private boolean isOwner;
    private OnPaperActionListener listener;

    public interface OnPaperActionListener {
        void onSetPriorityClick(TeamReadingListPaperResponse paper, int position);
        void onViewStatusClick(TeamReadingListPaperResponse paper);
        void onPaperClick(TeamReadingListPaperResponse paper);
        void onRemovePaperClick(TeamReadingListPaperResponse paper, int position);
    }

    public TeamReadingListPaperAdapter(List<TeamReadingListPaperResponse> papers, boolean isOwner, OnPaperActionListener listener) {
        this.papers = papers;
        this.isOwner = isOwner;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reading_list_paper, parent, false);
        return new PaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaperViewHolder holder, int position) {
        TeamReadingListPaperResponse paper = papers.get(position);

        holder.tvPaperTitle.setText(paper.getTitle());
        holder.tvPaperAuthors.setText("Author: " + paper.getAuthorName());

        // Set priority chip
        String priority = paper.getPriority() != null ? paper.getPriority() : "MEDIUM";
        holder.chipPriority.setText(priority);

        switch (priority.toUpperCase()) {
            case "HIGH":
                holder.chipPriority.setChipBackgroundColorResource(android.R.color.holo_red_dark);
                break;
            case "LOW":
                holder.chipPriority.setChipBackgroundColorResource(android.R.color.darker_gray);
                break;
            case "MEDIUM":
            default:
                holder.chipPriority.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
                break;
        }

        // Show/hide owner-only buttons
        if (isOwner) {
            holder.btnSetPriority.setVisibility(View.VISIBLE);
            holder.btnRemove.setVisibility(View.VISIBLE);

            holder.btnSetPriority.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSetPriorityClick(paper, holder.getAdapterPosition());
                }
            });

            holder.btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemovePaperClick(paper, holder.getAdapterPosition());
                }
            });
        } else {
            holder.btnSetPriority.setVisibility(View.GONE);
            holder.btnRemove.setVisibility(View.GONE);
            holder.btnViewStatus.setVisibility(View.GONE);
        }

        // View Status button - available for all users
        holder.btnViewStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewStatusClick(paper);
            }
        });

        // Click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaperClick(paper);
            }
        });
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    public void updatePapers(List<TeamReadingListPaperResponse> newPapers) {
        this.papers = newPapers;
        notifyDataSetChanged();
    }

    static class PaperViewHolder extends RecyclerView.ViewHolder {
        TextView tvPaperTitle;
        TextView tvPaperAuthors;
        Chip chipPriority;
        ImageButton btnSetPriority;
        ImageButton  btnViewStatus;
        ImageButton  btnRemove;

        public PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaperTitle = itemView.findViewById(R.id.tvPaperTitle);
            tvPaperAuthors = itemView.findViewById(R.id.tvPaperAuthors);
            chipPriority = itemView.findViewById(R.id.chipPriority);
            btnSetPriority = itemView.findViewById(R.id.btnSetPriority);
            btnViewStatus = itemView.findViewById(R.id.btnViewStatus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}