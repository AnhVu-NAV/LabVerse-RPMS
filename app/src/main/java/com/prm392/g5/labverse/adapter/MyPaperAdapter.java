package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.paper.PaperInfoResponse;

import java.util.ArrayList;
import java.util.List;

public class MyPaperAdapter extends RecyclerView.Adapter<MyPaperAdapter.ViewHolder> {

    public interface OnPaperClickListener {
        void onPaperClick(PaperInfoResponse paper);
    }

    private List<PaperInfoResponse> papers;
    private final OnPaperClickListener listener;

    public MyPaperAdapter(List<PaperInfoResponse> papers, OnPaperClickListener listener) {
        this.papers = papers != null ? papers : new ArrayList<>();
        this.listener = listener;
    }

    public void updatePapers(List<PaperInfoResponse> newPapers) {
        this.papers = newPapers != null ? newPapers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_paper_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaperInfoResponse paper = papers.get(position);

        holder.tvTitle.setText(paper.getTitle() != null ? paper.getTitle() : "Untitled");
        holder.tvAuthor.setText(
                paper.getAuthorName() != null ? paper.getAuthorName() : "Unknown author"
        );

        String year = paper.getPublicationYear() != null ? paper.getPublicationYear() : "";
        holder.tvYear.setText(year.isEmpty() ? "" : "Year: " + year);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaperClick(paper);
            }
        });
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvYear;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPaperTitle);
            tvAuthor = itemView.findViewById(R.id.tvPaperAuthor);
            tvYear = itemView.findViewById(R.id.tvPaperYear);
        }
    }
}
