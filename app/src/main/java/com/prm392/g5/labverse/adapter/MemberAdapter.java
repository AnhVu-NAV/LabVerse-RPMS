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

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<MemberResponse> members;
    private boolean isOwner;
    private OnMemberDeleteListener deleteListener;

    public interface OnMemberDeleteListener {
        void onDeleteClick(MemberResponse member, int position);
    }

    public MemberAdapter(List<MemberResponse> members, boolean isOwner) {
        this.members = members;
        this.isOwner = isOwner;
    }

    public void setOnMemberDeleteListener(OnMemberDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        MemberResponse member = members.get(position);

        holder.tvMemberName.setText(member.getFullName());
        holder.tvMemberEmail.setText("Email: " + member.getEmail());
        holder.tvMemberRole.setText("Role: " + (member.getRole() != null ? member.getRole() : "N/A"));
        holder.tvMemberStatus.setText("Status: " + (member.getStatus() != null ? member.getStatus() : "N/A"));

        if (isOwner) {
            holder.btnDeleteMember.setVisibility(View.VISIBLE);
            holder.btnDeleteMember.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(member, holder.getAdapterPosition());
                }
            });
        } else {
            holder.btnDeleteMember.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvMemberEmail, tvMemberRole, tvMemberStatus;
        Button btnDeleteMember;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberEmail = itemView.findViewById(R.id.tvMemberEmail);
            tvMemberRole = itemView.findViewById(R.id.tvMemberRole);
            tvMemberStatus = itemView.findViewById(R.id.tvMemberStatus);
            btnDeleteMember = itemView.findViewById(R.id.btnDeleteMember);
        }
    }
}