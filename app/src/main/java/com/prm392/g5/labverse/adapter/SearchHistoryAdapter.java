package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.prm392.g5.labverse.R;
import java.util.ArrayList;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.SearchHistoryViewHolder> {

    private List<String> searchHistoryList;
    private OnSearchHistoryClickListener listener;

    public interface OnSearchHistoryClickListener {
        void onSearchHistoryClick(String query);
        void onRemoveClick(String query, int position);
    }

    public SearchHistoryAdapter(List<String> searchHistoryList, OnSearchHistoryClickListener listener) {
        this.searchHistoryList = searchHistoryList != null ? searchHistoryList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_search, parent, false);
        return new SearchHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistoryViewHolder holder, int position) {
        String query = searchHistoryList.get(position);
        holder.bind(query);
    }

    @Override
    public int getItemCount() {
        return searchHistoryList.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < searchHistoryList.size()) {
            searchHistoryList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateSearchHistory(List<String> newSearchHistory) {
        this.searchHistoryList = newSearchHistory != null ? newSearchHistory : new ArrayList<>();
        notifyDataSetChanged();
    }

    class SearchHistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSearchQuery;
        private ImageView ivRemove;

        public SearchHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSearchQuery = itemView.findViewById(R.id.tv_search_query);
            ivRemove = itemView.findViewById(R.id.iv_remove);
        }

        public void bind(String query) {
            tvSearchQuery.setText(query);

            // Click on the item to re-run the search
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSearchHistoryClick(query);
                }
            });

            // Click on remove button to delete from history
            ivRemove.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRemoveClick(query, position);
                    }
                }
            });
        }
    }
}

