package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.entity.Paper;

public class PaperResultAdapter extends ListAdapter<Paper, PaperResultAdapter.VH> {

    public interface OnPaperClick {
        void onClick(Paper p);
    }

    private final OnPaperClick onPaperClick;

    public PaperResultAdapter(java.util.List<Paper> init, OnPaperClick cb) {
        super(DIFF);
        this.onPaperClick = cb;
        submitList(init);
    }

    private static final DiffUtil.ItemCallback<Paper> DIFF = new DiffUtil.ItemCallback<>() {
        @Override public boolean areItemsTheSame(@NonNull Paper a, @NonNull Paper b) {
            return a.getId().equals(b.getId());
        }
        @Override public boolean areContentsTheSame(@NonNull Paper a, @NonNull Paper b) {
            return a.getTitle().equals(b.getTitle())
                    && eq(a.getAuthorName(), b.getAuthorName())
                    && eq(a.getJournalName(), b.getJournalName())
                    && eq(a.getPublicationYear(), b.getPublicationYear());
        }
        private boolean eq(Object x, Object y){ return x==null ? y==null : x.equals(y); }
    };

    @NonNull
    @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paper_result, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        Paper p = getItem(position);
        h.title.setText(p.getTitle() == null ? "(No title)" : p.getTitle());
        String author = p.getAuthorName() == null ? "" : p.getAuthorName();
        String journal = p.getJournalName() == null ? "" : p.getJournalName();
        String year = p.getPublicationYear() == null ? "" : p.getPublicationYear();
        h.meta.setText(joinMeta(author, journal, year));

        h.itemView.setOnClickListener(v -> {
            if (onPaperClick != null) onPaperClick.onClick(p);
        });
    }

    private String joinMeta(String author, String journal, String year) {
        java.util.ArrayList<String> parts = new java.util.ArrayList<>();
        if (!author.isEmpty()) parts.add(author);
        if (!journal.isEmpty()) parts.add(journal);
        if (!year.isEmpty()) parts.add(year);
        return String.join(" · ", parts);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title, meta;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.paper_title);
            meta = itemView.findViewById(R.id.paper_meta);
        }
    }
}
