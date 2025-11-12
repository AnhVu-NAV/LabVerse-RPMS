package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.search.SearchItem;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnPaperClick {
        void onPaper(SearchItem item);
    }
    public interface OnReadingListClick {
        void onReadingList(SearchItem item);
    }

    private static final int TYPE_PAPER = 1;
    private static final int TYPE_LIST  = 2;

    private final List<SearchItem> items = new ArrayList<>();
    private final OnPaperClick onPaperClick;
    private final OnReadingListClick onListClick;

    public SearchResultAdapter(OnPaperClick onPaperClick, OnReadingListClick onListClick) {
        this.onPaperClick = onPaperClick;
        this.onListClick  = onListClick;
    }

    public void submit(List<SearchItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @Override public int getItemCount() { return items.size(); }

    @Override public int getItemViewType(int position) {
        return (items.get(position).type == SearchItem.Type.PAPER) ? TYPE_PAPER : TYPE_LIST;
    }

    @NonNull
    @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_PAPER) {
            View v = inf.inflate(R.layout.item_search_paper, parent, false);
            return new PaperVH(v);
        } else {
            View v = inf.inflate(R.layout.item_search_reading_list, parent, false);
            return new ListVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        SearchItem it = items.get(position);
        if (h instanceof PaperVH) ((PaperVH) h).bind(it, onPaperClick);
        else ((ListVH) h).bind(it, onListClick);
    }

    static class PaperVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMeta;
        PaperVH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvMeta  = v.findViewById(R.id.tv_meta);
        }
        void bind(SearchItem it, OnPaperClick onClick) {
            tvTitle.setText(it.paper.getTitle());
            tvMeta.setText((it.paper.getAuthorName()==null?"":it.paper.getAuthorName())
                    + (it.paper.getPublicationYear()==null?"":" • " + it.paper.getPublicationYear()));
            itemView.setOnClickListener(v -> { if (onClick!=null) onClick.onPaper(it); });
        }
    }

    static class ListVH extends RecyclerView.ViewHolder {
        TextView tvName, tvMeta;
        ListVH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvMeta = v.findViewById(R.id.tv_meta);
        }
        void bind(SearchItem it, OnReadingListClick onClick) {
            tvName.setText(it.readingList.getName());
            // ví dụ meta: số paper nếu bạn có cột count, còn không thì để createdAt
            tvMeta.setText("");
            itemView.setOnClickListener(v -> { if (onClick!=null) onClick.onReadingList(it); });
        }
    }
}
