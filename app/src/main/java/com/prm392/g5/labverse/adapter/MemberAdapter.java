package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.team.MemberResponse;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    private List<MemberResponse> allMembers;
    private List<MemberResponse> filteredMembers;
    private String currentFilter = "ALL";
    private OnMemberActionListener listener;
    private boolean isOwner;

    public interface OnMemberActionListener {
        void onRemoveMember(MemberResponse member, int position);
    }

    public MemberAdapter(List<MemberResponse> members, boolean isOwner, OnMemberActionListener listener) {
        this.allMembers = new ArrayList<>(members);
        this.filteredMembers = new ArrayList<>(members);
        this.isOwner = isOwner;
        this.listener = listener;
    }

    // Constructor without listener
    public MemberAdapter(List<MemberResponse> members, boolean isOwner) {
        this(members, isOwner, null);
    }

    public void updateMembers(List<MemberResponse> newMembers) {
        this.allMembers = new ArrayList<>(newMembers);
        applyFilter(currentFilter);
    }

    public void applyFilter(String status) {
        this.currentFilter = status;
        filteredMembers.clear();

        if ("ALL".equals(status)) {
            filteredMembers.addAll(allMembers);
        } else {
            for (MemberResponse member : allMembers) {
                if (status.equals(member.getStatus())) {
                    filteredMembers.add(member);
                }
            }
        }

        notifyDataSetChanged();
    }

    public int getFilteredCount() {
        return filteredMembers.size();
    }

    public int getTotalCount() {
        return allMembers.size();
    }

    public int getCountByStatus(String status) {
        int count = 0;
        for (MemberResponse member : allMembers) {
            if (status.equals(member.getStatus())) {
                count++;
            }
        }
        return count;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemberResponse member = filteredMembers.get(position);

        holder.tvMemberName.setText(member.getFullName());
        holder.tvMemberEmail.setText(member.getEmail());
        holder.tvMemberRole.setText("Role: " + member.getRole());

        // Status
        String status = member.getStatus();
        holder.tvMemberStatus.setText(getStatusText(status));
        holder.tvMemberStatus.setTextColor(getStatusColor(holder, status));

        android.util.Log.d("MemberAdapter", "Member: " + member.getFullName() +
                ", Status: " + status + ", IsOwner: " + isOwner);

        // Show/hide remove button
        if (isOwner && "APPROVED".equals(status)) {
            holder.btnRemove.setVisibility(View.VISIBLE);
            holder.btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    int actualPosition = allMembers.indexOf(member);
                    listener.onRemoveMember(member, actualPosition);
                }
            });
        } else {
            holder.btnRemove.setVisibility(View.GONE);
        }
    }
    @Override
    public int getItemCount() {
        return filteredMembers.size();
    }

    private String getStatusText(String status) {
        switch (status) {
            case "APPROVED":
                return "Active";
            case "PENDING":
                return "Pending";
            case "REJECTED":
                return "Declined";
            default:
                return status;
        }
    }

    private int getStatusColor(ViewHolder holder, String status) {
        switch (status) {
            case "APPROVED":
                return holder.itemView.getContext().getColor(android.R.color.holo_green_dark);
            case "PENDING":
                return holder.itemView.getContext().getColor(android.R.color.holo_orange_dark);
            case "REJECTED":
                return holder.itemView.getContext().getColor(android.R.color.holo_red_dark);
            default:
                return holder.itemView.getContext().getColor(android.R.color.darker_gray);
        }
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvMemberEmail, tvMemberRole, tvMemberStatus;
        Button btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberEmail = itemView.findViewById(R.id.tvMemberEmail);
            tvMemberRole = itemView.findViewById(R.id.tvMemberRole);
            tvMemberStatus = itemView.findViewById(R.id.tvMemberStatus);
            btnRemove = itemView.findViewById(R.id.btnDeleteMember);
        }
    }
}