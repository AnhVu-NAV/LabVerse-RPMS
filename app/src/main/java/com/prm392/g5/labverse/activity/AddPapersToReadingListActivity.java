package com.prm392.g5.labverse.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.SelectablePaperAdapter;
import com.prm392.g5.labverse.viewmodel.ReadingListDetailViewModel;

import java.util.Set;

public class AddPapersToReadingListActivity extends BaseActivity {

    public static final String EXTRA_READING_LIST_ID = "reading_list_id";
    public static final String EXTRA_READING_LIST_NAME = "reading_list_name";

    private ReadingListDetailViewModel viewModel;
    private SelectablePaperAdapter adapter;
    private RecyclerView recyclerView;
    private MaterialToolbar toolbar;
    private MaterialButton addButton;
    private long readingListId;
    private String readingListName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_papers_to_reading_list);

        // Get reading list ID and name from intent
        readingListId = getIntent().getLongExtra(EXTRA_READING_LIST_ID, -1);
        readingListName = getIntent().getStringExtra(EXTRA_READING_LIST_NAME);

        if (readingListId == -1) {
            Toast.makeText(this, "Error: Invalid reading list", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.papers_recycler_view);
        addButton = findViewById(R.id.add_button);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Add to: " + (readingListName != null ? readingListName : "Reading List"));
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // Set up RecyclerView with adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectablePaperAdapter(selectedCount -> {
            // Enable/disable ADD button based on selection
            addButton.setEnabled(selectedCount > 0);
            if (selectedCount > 0) {
                addButton.setTextColor(0xFF1273D4); // Blue color
            } else {
                addButton.setTextColor(0xFF9E9E9E); // Gray color
            }
        });
        recyclerView.setAdapter(adapter);

        // Set up ViewModel
        viewModel = new ViewModelProvider(this).get(ReadingListDetailViewModel.class);

        // Observe all papers from the library
        viewModel.getAllPapers().observe(this, papers -> {
            if (papers != null) {
                adapter.setPapers(papers);
            }
        });

        // Set up ADD button click listener
        addButton.setOnClickListener(v -> {
            Set<String> selectedPaperIds = adapter.getSelectedPaperIds();
            if (selectedPaperIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one paper", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add selected papers to reading list
            for (String paperId : selectedPaperIds) {
                viewModel.addPaperToReadingList(readingListId, paperId);
            }

            Toast.makeText(this, selectedPaperIds.size() + " paper(s) added to reading list", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    protected int getSelectedNavigationItemId() {
        // Don't show bottom navigation on this screen
        return -1;
    }
}

