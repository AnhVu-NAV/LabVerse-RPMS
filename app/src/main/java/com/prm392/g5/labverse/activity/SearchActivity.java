package com.prm392.g5.labverse.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.SearchHistoryAdapter;
import com.prm392.g5.labverse.fragment.AdvancedFilterBottomSheet;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity implements SearchHistoryAdapter.OnSearchHistoryClickListener {

    private EditText etSearch;
    private ImageView ivBack;
    private ImageView ivClose;
    private ImageView ivClear;
    private LinearLayout chipAdvancedFilter;
    private RecyclerView rvRecentSearches;

    private SearchHistoryAdapter searchHistoryAdapter;
    private List<String> searchHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initializeViews();
        setupSearchHistory();
        setupListeners();
        setupBottomNavigation(R.id.navigation_explore);
    }

    private void initializeViews() {
        etSearch = findViewById(R.id.et_search);
        ivBack = findViewById(R.id.iv_back);
        ivClose = findViewById(R.id.iv_close);
        ivClear = findViewById(R.id.iv_clear);
        chipAdvancedFilter = findViewById(R.id.chip_advanced_filter);
        rvRecentSearches = findViewById(R.id.rv_recent_searches);
    }
    private void setupSearchHistory() {
        // Initialize sample search history
        searchHistoryList = new ArrayList<>();
        searchHistoryList.add(getString(R.string.search_history_ai_education));
        searchHistoryList.add(getString(R.string.search_history_sustainable_energy));
        searchHistoryList.add(getString(R.string.search_history_biotechnology));

        // Setup RecyclerView
        rvRecentSearches.setLayoutManager(new LinearLayoutManager(this));
        searchHistoryAdapter = new SearchHistoryAdapter(searchHistoryList, this);
        rvRecentSearches.setAdapter(searchHistoryAdapter);
    }

    private void setupListeners() {
        // Back button
        ivBack.setOnClickListener(v -> finish());

        // Close button
        ivClose.setOnClickListener(v -> finish());

        // Clear search button
        ivClear.setOnClickListener(v -> {
            etSearch.setText("");
            ivClear.setVisibility(View.GONE);
        });

        // Search input text watcher
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show/hide clear button based on text
                if (s.length() > 0) {
                    ivClear.setVisibility(View.VISIBLE);
                } else {
                    ivClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Search action on keyboard
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(etSearch.getText().toString());
                return true;
            }
            return false;
        });

        // Advanced filter chip
        chipAdvancedFilter.setOnClickListener(v -> showAdvancedFilterBottomSheet());
    }


    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add to search history if not already present
        if (!searchHistoryList.contains(query)) {
            searchHistoryList.add(0, query);
            searchHistoryAdapter.notifyDataSetChanged();
        }

        // TODO: Implement actual search functionality
        Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();

        // Hide keyboard
        etSearch.clearFocus();
    }

    @Override
    public void onSearchHistoryClick(String query) {
        // Re-run the search with the selected query
        etSearch.setText(query);
        performSearch(query);
    }

    @Override
    public void onRemoveClick(String query, int position) {
        // Remove from search history
        searchHistoryAdapter.removeItem(position);
        Toast.makeText(this, "Removed from search history", Toast.LENGTH_SHORT).show();
    }

    private void showAdvancedFilterBottomSheet() {
        AdvancedFilterBottomSheet bottomSheet = new AdvancedFilterBottomSheet();
        bottomSheet.setOnFilterAppliedListener((author, journal, tag, year) -> {
            // Handle filter application
            StringBuilder filterQuery = new StringBuilder("Filters:");
            if (!author.isEmpty()) filterQuery.append(" Author=").append(author);
            if (!journal.isEmpty()) filterQuery.append(" Journal=").append(journal);
            if (!tag.isEmpty()) filterQuery.append(" Tag=").append(tag);
            if (!year.isEmpty()) filterQuery.append(" Year=").append(year);

            // TODO: Apply filters to search results
            Toast.makeText(SearchActivity.this, filterQuery.toString(), Toast.LENGTH_SHORT).show();
        });
        bottomSheet.show(getSupportFragmentManager(), "AdvancedFilterBottomSheet");
    }

    @Override
    protected int getSelectedNavigationItemId() {
        return R.id.navigation_explore;
    }
}

