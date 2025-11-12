package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.entity.ReadingList;
import com.prm392.g5.labverse.entity.ReadingListItem;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReadingListAdapter extends RecyclerView.Adapter<ReadingListAdapter.ReadingListViewHolder> {

    private List<ReadingList> readingLists = new ArrayList<>();
    private OnReadingListClickListener listener;
    private boolean selectionMode = false;
    private Set<Integer> selectedPositions = new HashSet<>();

    private OnReadingListLongClickListener longClickListener;

    public interface OnReadingListLongClickListener {
        void onReadingListLongClick(ReadingList readingList, int position, View anchor);
    }

    public interface OnReadingListClickListener {
        void onReadingListClick(ReadingList readingList, int position);
    }

    public void setOnReadingListLongClickListener(OnReadingListLongClickListener l) {
        this.longClickListener = l;
    }

    public ReadingListAdapter(OnReadingListClickListener listener) {
        this.listener = listener;
    }

    public void setSelectionMode(boolean enabled) {
        this.selectionMode = enabled;
        if (!enabled) {
            selectedPositions.clear();
        }
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        notifyItemChanged(position);
    }

    public void selectAll() {
        selectedPositions.clear();
        for (int i = 0; i < readingLists.size(); i++) {
            selectedPositions.add(i);
        }
        notifyDataSetChanged();
    }

    public void deselectAll() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedPositions.size();
    }

    public List<ReadingList> getSelectedItems() {
        List<ReadingList> selected = new ArrayList<>();
        for (int position : selectedPositions) {
            if (position < readingLists.size()) {
                selected.add(readingLists.get(position));
            }
        }
        return selected;
    }

    public void removeSelectedItems() {
        List<ReadingList> itemsToRemove = new ArrayList<>();
        for (int position : selectedPositions) {
            if (position < readingLists.size()) {
                itemsToRemove.add(readingLists.get(position));
            }
        }
        readingLists.removeAll(itemsToRemove);
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReadingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reading_list_card, parent, false);
        return new ReadingListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingListViewHolder holder, int position) {
        ReadingList readingList = readingLists.get(position);
        boolean isSelected = selectedPositions.contains(position);
        holder.bind(readingList, position, selectionMode, isSelected);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(position);
                if (listener != null) {
                    listener.onReadingListClick(readingList, position);
                }
            } else {
                if (listener != null) {
                    listener.onReadingListClick(readingList, position);
                }
            }
        });

        // LONG-CLICK: gọi ra Activity để show PopupMenu (Edit/Delete)
        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return false;
            if (longClickListener != null) {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                // anchor: dùng itemView hoặc thumbnail nếu muốn menu nằm cạnh ảnh
                View anchor = (holder.thumbnailImageView != null) ? holder.thumbnailImageView : holder.itemView;
                longClickListener.onReadingListLongClick(readingList, pos, anchor);
                return true;
            }
            return false;
        });

        // Checkbox click: chỉ phục vụ selection mode
        if (holder.checkbox != null) {
            holder.checkbox.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                toggleSelection(pos);
                if (listener != null) {
                    listener.onReadingListClick(readingList, pos);
                }
            });
        }
    }

    public interface OnListActionsListener {
        void onShowListMenu(View anchor, ReadingListItem item, int position);
    }

    @Override
    public int getItemCount() {
        return readingLists.size();
    }

    public void setReadingLists(List<ReadingList> readingLists) {
        this.readingLists = readingLists;
        notifyDataSetChanged();
    }

    class ReadingListViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView metadataTextView;
        private final ImageView thumbnailImageView;
        private final MaterialCheckBox checkbox;

        public ReadingListViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.reading_list_title);
            metadataTextView = itemView.findViewById(R.id.reading_list_metadata);
            thumbnailImageView = itemView.findViewById(R.id.reading_list_thumbnail);
            checkbox = itemView.findViewById(R.id.reading_list_checkbox);
        }

        public void bind(ReadingList readingList, int position, boolean selectionMode, boolean isSelected) {
            titleTextView.setText(readingList.getName());

            // Format metadata: "X papers · Created on YYYY-MM-DD"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = readingList.getCreatedAt().format(formatter);
            String metadata = readingList.getPaperCount() + " papers · Created on " + formattedDate;
            metadataTextView.setText(metadata);

            // Set thumbnail based on position (cycling through different placeholders)
            int[] thumbnails = {
                    R.drawable.ic_paper_placeholder,
                    R.drawable.ic_plant_placeholder,
                    R.drawable.ic_paper_placeholder
            };
            thumbnailImageView.setImageResource(thumbnails[position % thumbnails.length]);

            // Handle selection mode UI
            if (checkbox != null) {
                checkbox.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
                checkbox.setChecked(isSelected);
            }
        }
    }
}
