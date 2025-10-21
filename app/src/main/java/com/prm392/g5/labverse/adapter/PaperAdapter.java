package com.prm392.g5.labverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.prm392.g5.labverse.R;
import java.util.List;

public class PaperAdapter extends RecyclerView.Adapter<PaperAdapter.PaperViewHolder> {

    private List<PaperItem> papers;

    public static class PaperItem {
        public String title;
        public String authors;
        public String status;
        public int progress;
        public int thumbnailRes;

        public PaperItem(String title, String authors, String status, int progress, int thumbnailRes) {
            this.title = title;
            this.authors = authors;
            this.status = status;
            this.progress = progress;
            this.thumbnailRes = thumbnailRes;
        }
    }

    public PaperAdapter(List<PaperItem> papers) {
        this.papers = papers;
    }

    @NonNull
    @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paper_card, parent, false);
        return new PaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaperViewHolder holder, int position) {
        PaperItem paper = papers.get(position);
        holder.bind(paper);
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    static class PaperViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView authorsText;
        Chip statusChip;
        ProgressBar progressBar;
        ImageView thumbnail;

        PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.paper_title);
            authorsText = itemView.findViewById(R.id.paper_authors);
            statusChip = itemView.findViewById(R.id.paper_status_chip);
            progressBar = itemView.findViewById(R.id.paper_progress_bar);
            thumbnail = itemView.findViewById(R.id.paper_thumbnail);
        }

        void bind(PaperItem paper) {
            titleText.setText(paper.title);
            authorsText.setText(paper.authors);
            statusChip.setText(paper.status);

            if (paper.thumbnailRes != 0) {
                thumbnail.setImageResource(paper.thumbnailRes);
            }

            if (paper.status.equals("Reading") && paper.progress > 0) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(paper.progress);
            } else {
                progressBar.setVisibility(View.GONE);
            }

            // Set chip colors and icons based on status
            if (paper.status.equals("Unread")) {
                statusChip.setChipBackgroundColorResource(R.color.chip_unread_bg);
                statusChip.setTextColor(itemView.getContext().getColor(R.color.chip_unread_text));
                statusChip.setChipIcon(itemView.getContext().getDrawable(R.drawable.ic_chip_unread));
                statusChip.setChipIconVisible(true);
            } else if (paper.status.equals("Reading")) {
                statusChip.setChipBackgroundColorResource(R.color.chip_reading_bg);
                statusChip.setTextColor(itemView.getContext().getColor(R.color.chip_reading_text));
                statusChip.setChipIcon(itemView.getContext().getDrawable(R.drawable.ic_chip_reading));
                statusChip.setChipIconVisible(true);
            } else if (paper.status.equals("Finished")) {
                statusChip.setChipBackgroundColorResource(R.color.chip_finished_bg);
                statusChip.setTextColor(itemView.getContext().getColor(R.color.chip_finished_text));
                statusChip.setChipIcon(itemView.getContext().getDrawable(R.drawable.ic_chip_finished));
                statusChip.setChipIconVisible(true);
            }
        }
    }
}
