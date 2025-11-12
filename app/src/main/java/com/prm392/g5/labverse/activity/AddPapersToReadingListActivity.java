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
    private String readingListId;
    private String readingListName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_papers_to_reading_list);

        readingListId   = getIntent().getStringExtra(EXTRA_READING_LIST_ID);
        readingListName = getIntent().getStringExtra(EXTRA_READING_LIST_NAME);
        if (readingListId == null || readingListId.isEmpty()) {
            Toast.makeText(this, "Error: Invalid reading list", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.papers_recycler_view);
        addButton = findViewById(R.id.add_button);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Add to: " + (readingListName != null ? readingListName : "Reading List"));
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectablePaperAdapter(selectedCount -> {
            addButton.setEnabled(selectedCount > 0);
            addButton.setTextColor(selectedCount > 0 ? 0xFF1273D4 : 0xFF9E9E9E);
        });
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ReadingListDetailViewModel.class);

        // Dùng DTO từ server -> adapter đang nhận entity Paper -> map ở adapter (bạn đã có hàm)
        viewModel.getAllPapersFromServer().observe(this, dtoList -> {
            if (dtoList != null) {
                adapter.setPapers(
//                        com.prm392.g5.labverse.viewmodel.MapperUtils.mapDtoToPaper(dtoList) // nếu bạn muốn tách mapper
                        // hoặc dùng
                         ReadingListDetailViewModel.mapDtoToPaper(dtoList) //nếu bạn đã có helper tương tự
                );
            }
        });

        viewModel.fetchAllPapersForPicker();

        addButton.setOnClickListener(v -> {
            Set<String> selectedPaperIds = adapter.getSelectedPaperIds();
            if (selectedPaperIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one paper", Toast.LENGTH_SHORT).show();
                return;
            }
            // ❗ Không gọi API ở đây. Trả kết quả về màn cha để cha gọi add & reload.
            android.content.Intent data = new android.content.Intent();
            data.putStringArrayListExtra(
                    ReadingListDetailActivity.RESULT_SELECTED_PAPER_IDS,
                    new java.util.ArrayList<>(selectedPaperIds)
            );
            setResult(RESULT_OK, data);
            finish();
        });
    }

    @Override
    protected int getSelectedNavigationItemId() { return -1; }
}
