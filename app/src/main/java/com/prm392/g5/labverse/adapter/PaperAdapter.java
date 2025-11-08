package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.prm392.g5.labverse.R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PaperAdapter extends RecyclerView.Adapter<PaperAdapter.PaperViewHolder> {

    private List<PaperItem> papers;
    private OnPaperClickListener listener;
    private OnPaperLongClickListener longClickListener;
    private boolean selectionMode = false;
    private Set<Integer> selectedPositions = new HashSet<>();

    public interface OnPaperClickListener {
        void onPaperClick(PaperItem paper, int position);
    }

    public interface OnPaperLongClickListener {
        void onPaperLongClick(PaperItem paper, int position);
    }

    public static class PaperItem {
        public String title;
        public String authors;
        public String status;
        public int progress;
        public int thumbnailRes;
        public boolean isSelected = false;

        public PaperItem(String title, String authors, String status, int progress, int thumbnailRes) {
            this.title = title;
            this.authors = authors;
            this.status = status;
            this.progress = progress;
            this.thumbnailRes = thumbnailRes;
        }
    }

    public PaperAdapter(List<PaperItem> papers) {
        this.papers = papers;
    }

    public PaperAdapter(List<PaperItem> papers, OnPaperClickListener listener) {
        this.papers = papers;
        this.listener = listener;
    }

    public void setOnLongClickListener(OnPaperLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setSelectionMode(boolean enabled) {
        this.selectionMode = enabled;
        if (!enabled) {
            selectedPositions.clear();
            for (PaperItem paper : papers) {
                paper.isSelected = false;
            }
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public void toggleSelection(int position) {
        if (position >= 0 && position < papers.size()) {
            PaperItem paper = papers.get(position);
            paper.isSelected = !paper.isSelected;
            if (paper.isSelected) {
                selectedPositions.add(position);
            } else {
                selectedPositions.remove(position);
            }
            notifyItemChanged(position);
        }
    }

    public void selectAll() {
        selectedPositions.clear();
        for (int i = 0; i < papers.size(); i++) {
            papers.get(i).isSelected = true;
            selectedPositions.add(i);
        }
        notifyDataSetChanged();
    }

    public void deselectAll() {
        selectedPositions.clear();
        for (PaperItem paper : papers) {
            paper.isSelected = false;
        }
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedPositions.size();
    }

    public List<PaperItem> getSelectedItems() {
        List<PaperItem> selected = new ArrayList<>();
        for (int position : selectedPositions) {
            if (position < papers.size()) {
                selected.add(papers.get(position));
            }
        }
        return selected;
    }

    public Set<Integer> getSelectedPositions() {
        return new HashSet<>(selectedPositions);
    }

    public void removeSelectedItems() {
        List<PaperItem> itemsToRemove = new ArrayList<>();
        for (int position : selectedPositions) {
            if (position < papers.size()) {
                itemsToRemove.add(papers.get(position));
            }
        }
        papers.removeAll(itemsToRemove);
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paper_card, parent, false);
        return new PaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaperViewHolder holder, int position) {
        PaperItem paper = papers.get(position);
        holder.bind(paper, selectionMode);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(position);
                if (listener != null) {
                    listener.onPaperClick(paper, position);
                }
            } else {
                if (listener != null) {
                    listener.onPaperClick(paper, position);
                }
            }
        });

        // Set long click listener
        holder.itemView.setOnLongClickListener(v -> {
            if (!selectionMode && longClickListener != null) {
                longClickListener.onPaperLongClick(paper, position);
                return true;
            }
            return false;
        });

        // Handle checkbox clicks
        if (holder.checkbox != null) {
            holder.checkbox.setOnClickListener(v -> {
                toggleSelection(position);
                if (listener != null) {
                    listener.onPaperClick(paper, position);
                }
            });
        }

        // Handle arrow clicks - navigate to detail even in selection mode
        if (holder.arrowIcon != null) {
            holder.arrowIcon.setOnClickListener(v -> {
                if (!selectionMode && listener != null) {
                    listener.onPaperClick(paper, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    static class PaperViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView authorsText;
        Chip statusChip;
        ProgressBar progressBar;
        ImageView thumbnail;
        MaterialCheckBox checkbox;
        ImageView arrowIcon;

        PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.paper_title);
            authorsText = itemView.findViewById(R.id.paper_authors);
            statusChip = itemView.findViewById(R.id.paper_status_chip);
            progressBar = itemView.findViewById(R.id.paper_progress_bar);
            thumbnail = itemView.findViewById(R.id.paper_thumbnail);
            checkbox = itemView.findViewById(R.id.paper_checkbox);
            arrowIcon = itemView.findViewById(R.id.paper_arrow_icon);
        }

        void bind(PaperItem paper, boolean selectionMode) {
            titleText.setText(paper.title);
            authorsText.setText(paper.authors);
            statusChip.setText(paper.status);

            if (paper.thumbnailRes != 0) {
                thumbnail.setImageResource(paper.thumbnailRes);
            }

            if (paper.status.equals("Reading") && paper.progress > 0) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(paper.progress);
            } else {
                progressBar.setVisibility(View.GONE);
            }

            // Handle selection mode UI
            if (checkbox != null) {
                checkbox.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
                checkbox.setChecked(paper.isSelected);
            }

            if (arrowIcon != null) {
                arrowIcon.setVisibility(selectionMode ? View.VISIBLE : View.VISIBLE);
            }

            // Set chip colors and icons based on status
            if (paper.status.equals("Unread")) {
                statusChip.setChipBackgroundColorResource(R.color.chip_unread_bg);
                statusChip.setTextColor(itemView.getContext().getColor(R.color.chip_unread_text));
                statusChip.setChipIcon(itemView.getContext().getDrawable(R.drawable.ic_chip_unread));
                statusChip.setChipIconVisible(true);
            } else if (paper.status.equals("Reading")) {
                statusChip.setChipBackgroundColorResource(R.color.chip_reading_bg);
                statusChip.setTextColor(itemView.getContext().getColor(R.color.chip_reading_text));
                statusChip.setChipIcon(itemView.getContext().getDrawable(R.drawable.ic_chip_reading));
                statusChip.setChipIconVisible(true);
            } else if (paper.status.equals("Finished")) {
                statusChip.setChipBackgroundColorResource(R.color.chip_finished_bg);
                statusChip.setTextColor(itemView.getContext().getColor(R.color.chip_finished_text));
                statusChip.setChipIcon(itemView.getContext().getDrawable(R.drawable.ic_chip_finished));
                statusChip.setChipIconVisible(true);
            }
        }
    }
}
