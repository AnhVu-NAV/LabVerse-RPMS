package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.team.TeamReadingStatusResponse;

import java.util.ArrayList;
import java.util.List;

public class MemberReadingStatusAdapter extends RecyclerView.Adapter<MemberReadingStatusAdapter.ViewHolder> {

    private List<TeamReadingStatusResponse> statusList;

    public MemberReadingStatusAdapter(List<TeamReadingStatusResponse> statusList) {
        this.statusList = statusList != null ? statusList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_member_reading_status.xml để tạo view cho mỗi item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_reading_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeamReadingStatusResponse status = statusList.get(position);

        // Member info
        holder.tvMemberName.setText(
                status.getUserName() != null ? status.getUserName() : "Unknown User"
        );
        holder.tvMemberEmail.setText(
                status.getUserEmail() != null ? status.getUserEmail() : "No email"
        );

        // Xác định trạng thái trang
        String pageStatus;
        if (status.getCurrentPage() == 0) {
            pageStatus = "To Read"; // Nếu chưa đọc
        } else if (status.getCurrentPage() > 0 && status.getCurrentPage() < status.getTotalPage()) {
            pageStatus = "Reading"; // Đang đọc
        } else if (status.getCurrentPage() == status.getTotalPage()) {
            pageStatus = "Finished"; // Đã đọc xong
        } else {
            pageStatus = "Error"; // Trường hợp lỗi
        }

        holder.tvCurrentPage.setText(pageStatus); // Hiển thị trạng thái đọc thay vì số trang
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    public void updateStatuses(List<TeamReadingStatusResponse> newStatuses) {
        this.statusList = newStatuses != null ? newStatuses : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvMemberEmail, tvCurrentPage;

        ViewHolder(View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberEmail = itemView.findViewById(R.id.tvMemberEmail);
            tvCurrentPage = itemView.findViewById(R.id.tvCurrentPage);
        }
    }
}
