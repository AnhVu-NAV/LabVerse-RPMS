package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.entity.Paper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectablePaperAdapter extends RecyclerView.Adapter<SelectablePaperAdapter.PaperViewHolder> {

    private List<Paper> papers = new ArrayList<>();
    private Set<String> selectedPaperIds = new HashSet<>();
    private OnSelectionChangedListener selectionChangedListener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    public SelectablePaperAdapter(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    @NonNull
    @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selectable_paper, parent, false);
        return new PaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaperViewHolder holder, int position) {
        Paper paper = papers.get(position);
        holder.bind(paper, selectedPaperIds.contains(paper.getId()));
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    public void setPapers(List<Paper> papers) {
        this.papers = papers;
        notifyDataSetChanged();
    }

    public Set<String> getSelectedPaperIds() {
        return new HashSet<>(selectedPaperIds);
    }

    class PaperViewHolder extends RecyclerView.ViewHolder {
        private final ImageView paperIcon;
        private final TextView paperTitle;
        private final TextView paperPublication;
        private final CheckBox checkbox;

        public PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            paperIcon = itemView.findViewById(R.id.paper_icon);
            paperTitle = itemView.findViewById(R.id.paper_title);
            paperPublication = itemView.findViewById(R.id.paper_publication);
            checkbox = itemView.findViewById(R.id.checkbox);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Paper paper = papers.get(position);
                    toggleSelection(paper.getId());
                    checkbox.setChecked(selectedPaperIds.contains(paper.getId()));

                    // Update background to show selection
                    updateSelectionBackground(selectedPaperIds.contains(paper.getId()));

                    if (selectionChangedListener != null) {
                        selectionChangedListener.onSelectionChanged(selectedPaperIds.size());
                    }
                }
            });
        }

        public void bind(Paper paper, boolean isSelected) {
            // Set paper title (use ID as placeholder since Paper entity has limited fields)
            paperTitle.setText("Paper " + paper.getId());

            // Set publication info (placeholder)
            paperPublication.setText("Document ID: " + paper.getId());

            checkbox.setChecked(isSelected);
            updateSelectionBackground(isSelected);
        }

        private void updateSelectionBackground(boolean isSelected) {
            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                itemView.setAlpha(0.95f);
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                itemView.setAlpha(1.0f);
            }
        }

        private void toggleSelection(String paperId) {
            if (selectedPaperIds.contains(paperId)) {
                selectedPaperIds.remove(paperId);
            } else {
                selectedPaperIds.add(paperId);
            }
        }
    }
}

