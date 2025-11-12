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
    private final Set<Integer> selectedPositions = new HashSet<>();

    public interface OnPaperClickListener {
        void onPaperClick(PaperItem paper, int position);
    }

    public interface OnPaperLongClickListener {
        void onPaperLongClick(PaperItem paper, int position);
    }


    public static class PaperItem {
        public final String id;
        public String title;
        public String authors;
        public String journal;
        public String status;
        public int progress;
        public int thumbnailRes;
        public boolean isSelected = false;

        public PaperItem(String id, String title, String authors, String journal,
                         String status, int progress, int thumbnailRes) {
            this.id = id;
            this.title = title;
            this.authors = authors;
            this.journal = journal;
            this.status = status;
            this.progress = progress;
            this.thumbnailRes = thumbnailRes;
        }

        public PaperItem(String id, String title, String authors,
                         String status, int progress, int thumbnailRes) {
            this(id, title, authors, /*journal*/ null, status, progress, thumbnailRes);
        }
    }

    public PaperAdapter(List<PaperItem> papers) { this.papers = papers; }

    public PaperAdapter(List<PaperItem> papers, OnPaperClickListener listener) {
        this.papers = papers;
        this.listener = listener;
    }

    public void setOnLongClickListener(OnPaperLongClickListener listener) { this.longClickListener = listener; }

    public void setItems(List<PaperItem> newItems) {
        if (newItems == null) newItems = new ArrayList<>();
        this.papers = newItems;
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean enabled) {
        this.selectionMode = enabled;
        if (!enabled) {
            selectedPositions.clear();
            for (PaperItem paper : papers) paper.isSelected = false;
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() { return selectionMode; }

    public void toggleSelection(int position) {
        if (position >= 0 && position < papers.size()) {
            PaperItem paper = papers.get(position);
            paper.isSelected = !paper.isSelected;
            if (paper.isSelected) selectedPositions.add(position);
            else selectedPositions.remove(position);
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
        for (PaperItem p : papers) p.isSelected = false;
        notifyDataSetChanged();
    }

    public int getSelectedCount() { return selectedPositions.size(); }

    public List<PaperItem> getSelectedItems() {
        List<PaperItem> out = new ArrayList<>();
        for (int pos : selectedPositions) if (pos < papers.size()) out.add(papers.get(pos));
        return out;
    }

    public Set<Integer> getSelectedPositions() { return new HashSet<>(selectedPositions); }

    public void removeSelectedItems() {
        List<PaperItem> toRemove = new ArrayList<>();
        for (int pos : selectedPositions) if (pos < papers.size()) toRemove.add(papers.get(pos));
        papers.removeAll(toRemove);
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public interface OnPaperActionsListener {
        void onShowItemMenu(View anchor, PaperItem item, int position);
    }

    private OnPaperActionsListener actionsListener;
    public void setActionsListener(OnPaperActionsListener l) { this.actionsListener = l; }

    @NonNull @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paper_card, parent, false);
        return new PaperViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PaperViewHolder holder, int position) {
        PaperItem paper = papers.get(position);
        holder.bind(paper, selectionMode);

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (selectionMode) toggleSelection(pos);
            else if (listener != null) listener.onPaperClick(papers.get(pos), pos);
        });

        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return false;
            if (actionsListener != null) {
                actionsListener.onShowItemMenu(v, papers.get(pos), pos);
                return true;
            }
            return false;
        });

        if (holder.checkbox != null) {
            holder.checkbox.setOnCheckedChangeListener(null);
            holder.checkbox.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
            holder.checkbox.setChecked(paper.isSelected);
            holder.checkbox.setOnCheckedChangeListener((b, checked) -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                PaperItem p = papers.get(pos);
                p.isSelected = checked;
                if (checked) selectedPositions.add(pos); else selectedPositions.remove(pos);
                notifyItemChanged(pos);
            });
            holder.checkbox.setOnClickListener(v -> {/* handled above */});
        }

        if (holder.arrowIcon != null) {
            holder.arrowIcon.setVisibility(View.VISIBLE);
            holder.arrowIcon.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                if (!selectionMode && listener != null) listener.onPaperClick(papers.get(pos), pos);
                else if (selectionMode) toggleSelection(pos);
            });
        }
    }

    @Override public int getItemCount() { return papers == null ? 0 : papers.size(); }

    static class PaperViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, authorsText, journalText;
        Chip statusChip;
        ProgressBar progressBar;
        ImageView thumbnail, arrowIcon;
        MaterialCheckBox checkbox;

        PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText   = itemView.findViewById(R.id.paper_title);
            authorsText = itemView.findViewById(R.id.paper_authors);
            journalText = itemView.findViewById(R.id.paper_journal);
            statusChip  = itemView.findViewById(R.id.paper_status_chip);
            progressBar = itemView.findViewById(R.id.paper_progress_bar);
            thumbnail   = itemView.findViewById(R.id.paper_thumbnail);
            checkbox    = itemView.findViewById(R.id.paper_checkbox);
            arrowIcon   = itemView.findViewById(R.id.paper_arrow_icon);
        }

        void bind(PaperItem paper, boolean selectionMode) {
            titleText.setText(paper.title);
            authorsText.setText(paper.authors);

            if (journalText != null) {
                if (paper.journal == null || paper.journal.trim().isEmpty()) {
                    journalText.setVisibility(View.GONE);
                } else {
                    journalText.setVisibility(View.VISIBLE);
                    journalText.setText(paper.journal);
                }
            }

            statusChip.setText(paper.status);
            if (paper.thumbnailRes != 0) thumbnail.setImageResource(paper.thumbnailRes);
            else thumbnail.setImageResource(R.drawable.ic_paper_placeholder);

            if ((paper.status.equalsIgnoreCase("Reading") && paper.progress > 0) ||
                    paper.status.equalsIgnoreCase("Finished")) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(paper.status.equalsIgnoreCase("Finished") ? 100 : paper.progress);
            } else {
                progressBar.setVisibility(View.GONE);
            }

            if (checkbox != null) {
                checkbox.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
                checkbox.setChecked(paper.isSelected);
            }
            if (arrowIcon != null) arrowIcon.setVisibility(View.VISIBLE);

            if (paper.status.equalsIgnoreCase("Unread")) {
                statusChip.setChipBackgroundColorResource(R.color.chip_unread_bg);
                statusChip.setTextColor(itemView.getContext().getColor(R.color.chip_unread_text));
                statusChip.setChipIcon(itemView.getContext().getDrawable(R.drawable.ic_chip_unread));
            } else if (paper.status.equalsIgnoreCase("Reading")) {
                statusChip.setChipBackgroundColorResource(R.color.chip_reading_bg);
                statusChip.setTextColor(itemView.getContext().getColor(R.color.chip_reading_text));
                statusChip.setChipIcon(itemView.getContext().getDrawable(R.drawable.ic_chip_reading));
            } else {
                statusChip.setChipBackgroundColorResource(R.color.chip_finished_bg);
                statusChip.setTextColor(itemView.getContext().getColor(R.color.chip_finished_text));
                statusChip.setChipIcon(itemView.getContext().getDrawable(R.drawable.ic_chip_finished));
            }
            statusChip.setChipIconVisible(true);
        }
    }
}
