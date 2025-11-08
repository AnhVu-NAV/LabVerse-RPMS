package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.team.InvitationResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {

    private List<InvitationResponse> invitations;
    private OnInvitationActionListener listener;

    public interface OnInvitationActionListener {
        void onAcceptClick(InvitationResponse invitation, int position);
        void onRejectClick(InvitationResponse invitation, int position);
    }

    public InvitationAdapter(List<InvitationResponse> invitations, OnInvitationActionListener listener) {
        this.invitations = invitations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invitation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InvitationResponse invitation = invitations.get(position);

        holder.tvTeamName.setText(invitation.getTeamName());
        holder.tvTeamDescription.setText(invitation.getTeamDescription());
        holder.tvInvitedBy.setText("Invited by: " + invitation.getInvitedBy());

        // Format time
        String timeAgo = formatTimeAgo(invitation.getInvitedAt());
        holder.tvInvitedAt.setText("📅 " + timeAgo);

        // Days left
        int daysLeft = invitation.getDaysLeft();
        if (invitation.isExpired()) {
            holder.tvDaysLeft.setText("❌ Expired");
            holder.tvDaysLeft.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
            holder.btnAccept.setEnabled(false);
            holder.btnReject.setEnabled(false);
            holder.btnAccept.setAlpha(0.5f);
            holder.btnReject.setAlpha(0.5f);
        } else if (daysLeft <= 1) {
            holder.tvDaysLeft.setText("⚠️ " + daysLeft + " day left");
            holder.tvDaysLeft.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        } else {
            holder.tvDaysLeft.setText("⏰ " + daysLeft + " days left");
            holder.tvDaysLeft.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        }

        // Show NEW badge for recent invitations (< 1 hour)
        long hoursSinceInvited = getHoursSince(invitation.getInvitedAt());
        if (hoursSinceInvited < 1) {
            holder.tvBadgeNew.setVisibility(View.VISIBLE);
        } else {
            holder.tvBadgeNew.setVisibility(View.GONE);
        }

        // Button listeners
        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null && !invitation.isExpired()) {
                listener.onAcceptClick(invitation, holder.getAdapterPosition());
            }
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null && !invitation.isExpired()) {
                listener.onRejectClick(invitation, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return invitations.size();
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

    private long getHoursSince(String dateTimeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(dateTimeString);

            if (date == null) {
                return 999;
            }

            long diffInMillis = System.currentTimeMillis() - date.getTime();
            return TimeUnit.MILLISECONDS.toHours(diffInMillis);
        } catch (ParseException e) {
            e.printStackTrace();
            return 999;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBadgeNew, tvTeamName, tvTeamDescription, tvInvitedBy, tvInvitedAt, tvDaysLeft;
        Button btnAccept, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBadgeNew = itemView.findViewById(R.id.tvBadgeNew);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvTeamDescription = itemView.findViewById(R.id.tvTeamDescription);
            tvInvitedBy = itemView.findViewById(R.id.tvInvitedBy);
            tvInvitedAt = itemView.findViewById(R.id.tvInvitedAt);
            tvDaysLeft = itemView.findViewById(R.id.tvDaysLeft);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}