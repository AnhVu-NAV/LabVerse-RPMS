package com.prm392.g5.labverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.SearchHistoryAdapter;
import com.prm392.g5.labverse.adapter.SearchResultAdapter;
import com.prm392.g5.labverse.filter.PaperFilter;
import com.prm392.g5.labverse.fragment.AdvancedFilterBottomSheet;
import com.prm392.g5.labverse.search.SearchItem;
import com.prm392.g5.labverse.viewmodel.SearchViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity
        implements SearchHistoryAdapter.OnSearchHistoryClickListener {

    private EditText etSearch;
    private ImageView ivBack, ivClose, ivClear;
    private LinearLayout chipAdvancedFilter;
    private RecyclerView rvRecentSearches, rvResults;

    private SearchHistoryAdapter searchHistoryAdapter;
    private SearchResultAdapter resultsAdapter;
    private List<String> searchHistoryList;

    private SearchViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initializeViews();
        setupSearchHistory();
        setupResultsList();

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        viewModel.getResults().observe(this, items -> {
            resultsAdapter.submit(items);
            rvRecentSearches.setVisibility(View.GONE);
            rvResults.setVisibility(View.VISIBLE);
            // Optional toast:
            // Toast.makeText(this, "Found: " + (items==null?0:items.size()), Toast.LENGTH_SHORT).show();
        });

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
        rvResults = findViewById(R.id.rv_results);
    }

    private void setupSearchHistory() {
        searchHistoryList = new ArrayList<>();
        searchHistoryList.add(getString(R.string.search_history_ai_education));
        searchHistoryList.add(getString(R.string.search_history_sustainable_energy));
        searchHistoryList.add(getString(R.string.search_history_biotechnology));

        rvRecentSearches.setLayoutManager(new LinearLayoutManager(this));
        searchHistoryAdapter = new SearchHistoryAdapter(searchHistoryList, this);
        rvRecentSearches.setAdapter(searchHistoryAdapter);
        rvRecentSearches.setVisibility(View.VISIBLE);
    }

    private void setupResultsList() {
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        resultsAdapter = new SearchResultAdapter(
                // onPaperClick
                item -> {
                    Intent i = new Intent(this, PaperDetailActivity.class);
                    i.putExtra("paper_id", item.paper.getId()); // đảm bảo getId() có giá trị
                    startActivity(i);
                },
                // onReadingListClick
                item -> {
                    Intent i = new Intent(this, ReadingListDetailActivity.class);
                    i.putExtra("reading_list_id", item.readingList.getId());
                    startActivity(i);
                }
        );
        rvResults.setAdapter(resultsAdapter);
        rvResults.setVisibility(View.GONE);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        ivClose.setOnClickListener(v -> finish());

        ivClear.setOnClickListener(v -> {
            etSearch.setText("");
            ivClear.setVisibility(View.GONE);
            rvResults.setVisibility(View.GONE);
            rvRecentSearches.setVisibility(View.VISIBLE);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(etSearch.getText().toString());
                return true;
            }
            return false;
        });

        chipAdvancedFilter.setOnClickListener(v -> showAdvancedFilterBottomSheet());
    }

    private void performSearch(String query) {
        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_search_query, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!searchHistoryList.contains(q)) {
            searchHistoryList.add(0, q);
            searchHistoryAdapter.notifyDataSetChanged();
        }
        // không filter nâng cao → filter rỗng
        viewModel.search(q, new PaperFilter("", "", "", ""));
        etSearch.clearFocus();
    }

    @Override
    public void onSearchHistoryClick(String query) {
        etSearch.setText(query);
        performSearch(query);
    }

    @Override
    public void onRemoveClick(String query, int position) {
        searchHistoryAdapter.removeItem(position);
        Toast.makeText(this, R.string.removed_from_search_history, Toast.LENGTH_SHORT).show();
    }

    private void showAdvancedFilterBottomSheet() {
        AdvancedFilterBottomSheet bottomSheet = AdvancedFilterBottomSheet.newInstance(
                etSearch.getText() == null ? "" : etSearch.getText().toString()
        );
        bottomSheet.setOnFilterAppliedListener((author, journal, tag, year) -> {
            PaperFilter filter = new PaperFilter(author, journal, tag, year);
            String keyword = etSearch.getText() == null ? "" : etSearch.getText().toString().trim();
            viewModel.search(keyword, filter);
            rvRecentSearches.setVisibility(View.GONE);
            rvResults.setVisibility(View.VISIBLE);
            etSearch.clearFocus();
        });
        bottomSheet.show(getSupportFragmentManager(), "AdvancedFilterBottomSheet");
    }

    @Override
    protected int getSelectedNavigationItemId() {
        return R.id.navigation_explore;
    }
}
