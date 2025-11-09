package com.prm392.g5.labverse.adapter;

import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.team.TeamReadingListResponse;
import java.text.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

public class TeamReadingListAdapter extends RecyclerView.Adapter<TeamReadingListAdapter.ViewHolder> {

    private List<TeamReadingListResponse> lists;
    private final boolean isOwner;
    private final OnAction listener;

    public interface OnAction {
        void onItemClick(TeamReadingListResponse item);
        void onEditClick(TeamReadingListResponse item, int pos);
        void onDeleteClick(TeamReadingListResponse item, int pos);
    }

    public TeamReadingListAdapter(List<TeamReadingListResponse> lists, boolean isOwner, OnAction listener) {
        this.lists = lists;
        this.isOwner = isOwner;
        this.listener = listener;
    }

    public void submit(List<TeamReadingListResponse> newLists) {
        this.lists = newLists != null ? newLists : Collections.emptyList();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reading_list, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        TeamReadingListResponse it = lists.get(pos);
        h.tvName.setText(it.getName());
        h.tvDescription.setText(it.getDescription());
        h.tvDate.setText("Created: " + timeAgo(it.getCreatedAt()));
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClick(it); });

        if (isOwner) {
            h.btnMore.setVisibility(View.VISIBLE);
            h.btnMore.setOnClickListener(v -> showMenu(v, it, pos));
        } else h.btnMore.setVisibility(View.GONE);
    }

    @Override public int getItemCount() { return lists != null ? lists.size() : 0; }

    private void showMenu(View view, TeamReadingListResponse item, int pos) {
        android.widget.PopupMenu m = new android.widget.PopupMenu(view.getContext(), view);
        m.inflate(R.menu.menu_reading_list_item);
        m.setOnMenuItemClickListener(mi -> {
            int id = mi.getItemId();
            if (id == R.id.action_edit) { if (listener != null) listener.onEditClick(item, pos); return true; }
            if (id == R.id.action_delete) { if (listener != null) listener.onDeleteClick(item, pos); return true; }
            return false;
        });
        m.show();
    }

    private String timeAgo(String iso) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date d = sdf.parse(iso);
            if (d == null) return "Unknown";
            long diff = System.currentTimeMillis() - d.getTime();
            long min = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hr  = TimeUnit.MILLISECONDS.toHours(diff);
            long day = TimeUnit.MILLISECONDS.toDays(diff);
            if (min < 1) return "Just now";
            if (min < 60) return min + " minute" + (min>1?"s":"") + " ago";
            if (hr  < 24) return hr  + " hour"   + (hr>1 ?"s":"") + " ago";
            return day + " day" + (day>1?"s":"") + " ago";
        } catch (ParseException e) { return "Unknown"; }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvDate;
        ImageButton btnMore;
        ViewHolder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvReadingListName);
            tvDescription = v.findViewById(R.id.tvReadingListDescription);
            tvDate = v.findViewById(R.id.tvReadingListDate);
            btnMore = v.findViewById(R.id.btnMore);
        }
    }
}
