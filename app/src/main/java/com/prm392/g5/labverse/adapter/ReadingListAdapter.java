package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.team.TeamReadingListResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ReadingListAdapter extends RecyclerView.Adapter<ReadingListAdapter.ViewHolder> {

    private List<TeamReadingListResponse> readingLists;
    private OnReadingListActionListener listener;
    private boolean isOwner;

    public interface OnReadingListActionListener {
        void onItemClick(TeamReadingListResponse readingList);
        void onEditClick(TeamReadingListResponse readingList, int position);
        void onDeleteClick(TeamReadingListResponse readingList, int position);
    }

    public ReadingListAdapter(List<TeamReadingListResponse> readingLists, boolean isOwner, OnReadingListActionListener listener) {
        this.readingLists = readingLists;
        this.isOwner = isOwner;
        this.listener = listener;
    }

    public void updateReadingLists(List<TeamReadingListResponse> newReadingLists) {
        this.readingLists = newReadingLists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reading_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeamReadingListResponse readingList = readingLists.get(position);

        holder.tvName.setText(readingList.getName());
        holder.tvDescription.setText(readingList.getDescription());

        // Format date
        String dateText = "Created: " + formatTimeAgo(readingList.getCreatedAt());
        holder.tvDate.setText(dateText);

        // Click to view details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(readingList);
            }
        });

        // Show more button only for owner
        if (isOwner) {
            holder.btnMore.setVisibility(View.VISIBLE);
            holder.btnMore.setOnClickListener(v -> showPopupMenu(v, readingList, position));
        } else {
            holder.btnMore.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return readingLists.size();
    }

    private void showPopupMenu(View view, TeamReadingListResponse readingList, int position) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.menu_reading_list_item);

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_edit) {
                if (listener != null) {
                    listener.onEditClick(readingList, position);
                }
                return true;
            } else if (id == R.id.action_delete) {
                if (listener != null) {
                    listener.onDeleteClick(readingList, position);
                }
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private String formatTimeAgo(String dateTimeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(dateTimeString);

            if (date == null) {
                return "Unknown";
            }

            long diffInMillis = System.currentTimeMillis() - date.getTime();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            if (minutes < 1) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
            } else if (hours < 24) {
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else {
                return days + " day" + (days > 1 ? "s" : "") + " ago";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvDate;
        ImageButton btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvReadingListName);
            tvDescription = itemView.findViewById(R.id.tvReadingListDescription);
            tvDate = itemView.findViewById(R.id.tvReadingListDate);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}