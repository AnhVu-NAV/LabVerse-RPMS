package com.prm392.g5.labverse.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.PaperAdapter;
import com.prm392.g5.labverse.viewmodel.ReadingListDetailViewModel;

import java.util.ArrayList;
import java.util.List;

public class ReadingListDetailActivity extends BaseActivity {

    public static final String EXTRA_READING_LIST_ID = "reading_list_id";
    public static final String EXTRA_READING_LIST_NAME = "reading_list_name";

    private ReadingListDetailViewModel viewModel;
    private PaperAdapter adapter;
    private RecyclerView recyclerView;
    private MaterialToolbar toolbar;
    private long readingListId;
    private String readingListName;

    // Sort options
    private enum SortBy {
        CREATOR("Creator", "Sort By: Creator"),
        DATE("Date", "Sort By: Date"),
        DATE_ADDED("Date Added", "Sort By: Date Added"),
        PUBLICATION_TITLE("Publication Title", "Sort By: Publication Title"),
        TITLE("Title", "Sort By: Title"),
        YEAR("Year", "Sort By: Year");

        private final String name;
        private final String displayName;

        SortBy(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private enum SortOrder {
        ASCENDING, DESCENDING
    }

    private SortBy currentSortBy = SortBy.TITLE;
    private SortOrder currentSortOrder = SortOrder.ASCENDING;
    private final List<PaperAdapter.PaperItem> currentPaperItems = new ArrayList<>();
    private boolean isSelectionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_list_detail);

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
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_paper);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(readingListName != null ? readingListName : getString(R.string.reading_list));
        }

        toolbar.setNavigationOnClickListener(v -> finish());


        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaperAdapter(new ArrayList<>(), (paper, position) -> {
            if (isSelectionMode) {
                updateSelectionModeUI();
            } else {
                // TODO: Navigate to Paper Detail
                Toast.makeText(this, "Opening paper: " + paper.title, Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        // Handle back press for selection mode
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSelectionMode) {
                    exitSelectionMode();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Set up ViewModel
        viewModel = new ViewModelProvider(this).get(ReadingListDetailViewModel.class);
        viewModel.setReadingListId(readingListId);

        // Load mock test data for visual testing
        loadMockTestData();

        // Observe papers in reading list
        viewModel.getPapersInReadingList().observe(this, papers -> {
            if (papers != null && !papers.isEmpty()) {
                // Convert Paper entities to PaperItem for adapter
                currentPaperItems.clear();
                for (int i = 0; i < papers.size(); i++) {
                    // Create mock data for now - you'll need to extend Paper entity with more fields
                    currentPaperItems.add(new PaperAdapter.PaperItem(
                            "Paper " + papers.get(i).getId(),
                            "Authors",
                            "Unread",
                            0,
                            R.drawable.ic_paper_placeholder
                    ));
                }
                sortAndUpdateList();
            }
        });

        // Set up FAB click listener to show add paper dialog
        fabAdd.setOnClickListener(v -> showAddPaperDialog());

        // Set up bottom navigation
        setupBottomNavigation(R.id.navigation_reading_list);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        menu.clear();
        if (isSelectionMode) {
            getMenuInflater().inflate(R.menu.menu_selection_mode, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_reading_list_detail, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        if (isSelectionMode) {
            int selectedCount = adapter != null ? adapter.getSelectedCount() : 0;
            android.view.MenuItem selectAllItem = menu.findItem(R.id.action_select_all);
            if (selectAllItem != null) {
                if (selectedCount == currentPaperItems.size() && selectedCount > 0) {
                    selectAllItem.setTitle(R.string.deselect_all);
                } else {
                    selectAllItem.setTitle(R.string.select_all);
                }
                selectAllItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS | android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }
            android.view.MenuItem deleteItem = menu.findItem(R.id.action_delete);
            if (deleteItem != null) {
                deleteItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        if (isSelectionMode) {
            if (item.getItemId() == R.id.action_select_all) {
                handleSelectAllToggle();
                invalidateOptionsMenu();
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
        if (item.getItemId() == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (item.getItemId() == R.id.action_more) {
            showMoreOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show popup menu with more options including Delete
     */
    private void showMoreOptionsMenu() {
        View moreButton = toolbar.findViewById(R.id.action_more);
        if (moreButton == null) {
            moreButton = toolbar;
        }

        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(this, moreButton);
        popupMenu.getMenuInflater().inflate(R.menu.menu_reading_list_more, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.action_delete_mode) {
                enterSelectionMode();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void showAddPaperDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_add_paper);

        MaterialButton importFileButton = dialog.findViewById(R.id.import_file_button);
        MaterialButton addPaperButton = dialog.findViewById(R.id.add_paper_button);

        if (importFileButton != null) {
            importFileButton.setOnClickListener(v -> {
                Toast.makeText(this, "Import File - Coming Soon", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }
        if (addPaperButton != null) {
            addPaperButton.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(this, AddPapersToReadingListActivity.class);
                intent.putExtra(AddPapersToReadingListActivity.EXTRA_READING_LIST_ID, readingListId);
                intent.putExtra(AddPapersToReadingListActivity.EXTRA_READING_LIST_NAME, readingListName);
                startActivity(intent);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void showSortDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_sort);

        TextView sortByLabel = dialog.findViewById(R.id.sort_by_label);
        LinearLayout sortByRow = dialog.findViewById(R.id.sort_by_row);
        MaterialButton btnAscending = dialog.findViewById(R.id.btn_ascending);
        MaterialButton btnDescending = dialog.findViewById(R.id.btn_descending);

        if (sortByLabel != null) {
            sortByLabel.setText(currentSortBy.getDisplayName());
        }

        if (btnAscending != null && btnDescending != null) {
            updateSortOrderButtons(btnAscending, btnDescending);
        }

        if (sortByRow != null) {
            sortByRow.setOnClickListener(v -> showSortByCriteriaDialog(dialog));
        }

        if (btnAscending != null) {
            btnAscending.setOnClickListener(v -> {
                currentSortOrder = SortOrder.ASCENDING;
                if (btnDescending != null) updateSortOrderButtons(btnAscending, btnDescending);
                sortAndUpdateList();
            });
        }

        if (btnDescending != null) {
            btnDescending.setOnClickListener(v -> {
                currentSortOrder = SortOrder.DESCENDING;
                if (btnAscending != null) updateSortOrderButtons(btnAscending, btnDescending);
                sortAndUpdateList();
            });
        }

        dialog.show();
    }

    private void showSortByCriteriaDialog(BottomSheetDialog parentDialog) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_sort_by);

        // Get all views
        View btnBack = dialog.findViewById(R.id.btn_back);
        LinearLayout sortOptionCreator = dialog.findViewById(R.id.sort_option_creator);
        LinearLayout sortOptionDate = dialog.findViewById(R.id.sort_option_date);
        LinearLayout sortOptionDateAdded = dialog.findViewById(R.id.sort_option_date_added);
        LinearLayout sortOptionPublicationTitle = dialog.findViewById(R.id.sort_option_publication_title);
        LinearLayout sortOptionTitle = dialog.findViewById(R.id.sort_option_title);
        LinearLayout sortOptionYear = dialog.findViewById(R.id.sort_option_year);

        android.widget.ImageView checkCreator = dialog.findViewById(R.id.check_creator);
        android.widget.ImageView checkDate = dialog.findViewById(R.id.check_date);
        android.widget.ImageView checkDateAdded = dialog.findViewById(R.id.check_date_added);
        android.widget.ImageView checkPublicationTitle = dialog.findViewById(R.id.check_publication_title);
        android.widget.ImageView checkTitle = dialog.findViewById(R.id.check_title);
        android.widget.ImageView checkYear = dialog.findViewById(R.id.check_year);

        // Update checkmarks based on current selection
        updateSortByCheckmarks(checkCreator, checkDate, checkDateAdded, checkPublicationTitle, checkTitle, checkYear);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                dialog.dismiss();
                parentDialog.show();
            });
        }

        if (sortOptionCreator != null) {
            sortOptionCreator.setOnClickListener(v -> {
                currentSortBy = SortBy.CREATOR;
                sortAndUpdateList();
                dialog.dismiss();
                updateParentDialogSortByLabel(parentDialog);
                parentDialog.show();
            });
        }

        if (sortOptionDate != null) {
            sortOptionDate.setOnClickListener(v -> {
                currentSortBy = SortBy.DATE;
                sortAndUpdateList();
                dialog.dismiss();
                updateParentDialogSortByLabel(parentDialog);
                parentDialog.show();
            });
        }

        if (sortOptionDateAdded != null) {
            sortOptionDateAdded.setOnClickListener(v -> {
                currentSortBy = SortBy.DATE_ADDED;
                sortAndUpdateList();
                dialog.dismiss();
                updateParentDialogSortByLabel(parentDialog);
                parentDialog.show();
            });
        }

        if (sortOptionPublicationTitle != null) {
            sortOptionPublicationTitle.setOnClickListener(v -> {
                currentSortBy = SortBy.PUBLICATION_TITLE;
                sortAndUpdateList();
                dialog.dismiss();
                updateParentDialogSortByLabel(parentDialog);
                parentDialog.show();
            });
        }

        if (sortOptionTitle != null) {
            sortOptionTitle.setOnClickListener(v -> {
                currentSortBy = SortBy.TITLE;
                sortAndUpdateList();
                dialog.dismiss();
                updateParentDialogSortByLabel(parentDialog);
                parentDialog.show();
            });
        }

        if (sortOptionYear != null) {
            sortOptionYear.setOnClickListener(v -> {
                currentSortBy = SortBy.YEAR;
                sortAndUpdateList();
                dialog.dismiss();
                updateParentDialogSortByLabel(parentDialog);
                parentDialog.show();
            });
        }

        dialog.show();
    }

    private void updateSortByCheckmarks(android.widget.ImageView checkCreator,
                                       android.widget.ImageView checkDate,
                                       android.widget.ImageView checkDateAdded,
                                       android.widget.ImageView checkPublicationTitle,
                                       android.widget.ImageView checkTitle,
                                       android.widget.ImageView checkYear) {
        // Hide all checkmarks safely
        if (checkCreator != null) checkCreator.setVisibility(View.GONE);
        if (checkDate != null) checkDate.setVisibility(View.GONE);
        if (checkDateAdded != null) checkDateAdded.setVisibility(View.GONE);
        if (checkPublicationTitle != null) checkPublicationTitle.setVisibility(View.GONE);
        if (checkTitle != null) checkTitle.setVisibility(View.GONE);
        if (checkYear != null) checkYear.setVisibility(View.GONE);

        // Show checkmark for current selection safely
        switch (currentSortBy) {
            case CREATOR:
                if (checkCreator != null) checkCreator.setVisibility(View.VISIBLE);
                break;
            case DATE:
                if (checkDate != null) checkDate.setVisibility(View.VISIBLE);
                break;
            case DATE_ADDED:
                if (checkDateAdded != null) checkDateAdded.setVisibility(View.VISIBLE);
                break;
            case PUBLICATION_TITLE:
                if (checkPublicationTitle != null) checkPublicationTitle.setVisibility(View.VISIBLE);
                break;
            case TITLE:
                if (checkTitle != null) checkTitle.setVisibility(View.VISIBLE);
                break;
            case YEAR:
                if (checkYear != null) checkYear.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateParentDialogSortByLabel(BottomSheetDialog parentDialog) {
        View parentView = parentDialog.findViewById(R.id.sort_by_label);
        if (parentView instanceof TextView) {
            ((TextView) parentView).setText(currentSortBy.getDisplayName());
        }
    }

    private void updateSortOrderButtons(MaterialButton btnAscending, MaterialButton btnDescending) {
        if (currentSortOrder == SortOrder.ASCENDING) {
            btnAscending.setSelected(true);
            btnDescending.setSelected(false);
        } else {
            btnAscending.setSelected(false);
            btnDescending.setSelected(true);
        }
    }

    private void sortAndUpdateList() {
        if (currentPaperItems.isEmpty()) {
            return;
        }

        List<PaperAdapter.PaperItem> sortedItems = new ArrayList<>(currentPaperItems);

        switch (currentSortBy) {
            case CREATOR:
                sortedItems.sort((o1, o2) -> {
                    int result = o1.authors.compareToIgnoreCase(o2.authors);
                    return currentSortOrder == SortOrder.ASCENDING ? result : -result;
                });
                break;
            case DATE:
                // Sort by year (using progress field as year)
                sortedItems.sort((o1, o2) -> {
                    int result = Integer.compare(o1.progress, o2.progress);
                    return currentSortOrder == SortOrder.ASCENDING ? result : -result;
                });
                break;
            case DATE_ADDED:
                // Sort by date added (using title length as placeholder for demo)
                sortedItems.sort((o1, o2) -> {
                    int result = Integer.compare(o1.title.length(), o2.title.length());
                    return currentSortOrder == SortOrder.ASCENDING ? result : -result;
                });
                break;
            case PUBLICATION_TITLE:
                // Sort by publication title (using status as placeholder)
                sortedItems.sort((o1, o2) -> {
                    int result = o1.status.compareToIgnoreCase(o2.status);
                    return currentSortOrder == SortOrder.ASCENDING ? result : -result;
                });
                break;
            case TITLE:
                sortedItems.sort((o1, o2) -> {
                    int result = o1.title.compareToIgnoreCase(o2.title);
                    return currentSortOrder == SortOrder.ASCENDING ? result : -result;
                });
                break;
            case YEAR:
                sortedItems.sort((o1, o2) -> {
                    int result = Integer.compare(o1.progress, o2.progress);
                    return currentSortOrder == SortOrder.ASCENDING ? result : -result;
                });
                break;
        }

        adapter = new PaperAdapter(sortedItems, (paper, position) -> Toast.makeText(this, "Opening paper: " + paper.title, Toast.LENGTH_SHORT).show());
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected int getSelectedNavigationItemId() {
        return R.id.navigation_reading_list;
    }

    /**
     * Enter selection mode - allows multi-select of papers
     */
    private void enterSelectionMode() {
        isSelectionMode = true;
        adapter.setSelectionMode(true);
        updateSelectionModeUI();
        invalidateOptionsMenu(); // Refresh menu
    }

    /**
     * Exit selection mode - returns to normal view
     */
    private void exitSelectionMode() {
        isSelectionMode = false;
        adapter.setSelectionMode(false);
        updateNormalModeUI();
        invalidateOptionsMenu(); // Refresh menu
    }

    /**
     * Update toolbar UI for selection mode
     */
    private void updateSelectionModeUI() {
        int selectedCount = adapter.getSelectedCount();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(selectedCount + " Selected");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(v -> exitSelectionMode());
        invalidateOptionsMenu();
    }

    /**
     * Update toolbar UI for normal mode
     */
    private void updateNormalModeUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(readingListName != null ? readingListName : getString(R.string.reading_list));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        invalidateOptionsMenu();
    }

    /**
     * Toggle between Select All and Deselect All
     */
    private void handleSelectAllToggle() {
        int selectedCount = adapter.getSelectedCount();
        if (selectedCount == currentPaperItems.size() && selectedCount > 0) {
            adapter.deselectAll();
        } else {
            adapter.selectAll();
        }
        updateSelectionModeUI();
    }

    /**
     * Show confirmation dialog before deleting selected papers
     */
    private void showDeleteConfirmationDialog() {
        int selectedCount = adapter.getSelectedCount();

        if (selectedCount == 0) {
            Toast.makeText(this, "No papers selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_deletion, null);
        TextView messageText = dialogView.findViewById(R.id.dialog_message);
        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        com.google.android.material.button.MaterialButton btnDelete = dialogView.findViewById(R.id.btn_delete);

        // Set custom message
        String message = "Are you sure you want to delete " + selectedCount + " selected paper" + (selectedCount > 1 ? "s" : "") + "?";
        messageText.setText(message);

        // Create dialog
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Make dialog background transparent for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Set button click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            deleteSelectedPapers();
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Delete selected papers from the list
     */
    private void deleteSelectedPapers() {
        int selectedCount = adapter.getSelectedCount();

        // Remove selected items from the current list
        List<PaperAdapter.PaperItem> selectedItems = adapter.getSelectedItems();
        currentPaperItems.removeAll(selectedItems);

        // Update adapter
        adapter.removeSelectedItems();

        // Show confirmation
        Toast.makeText(this, selectedCount + " paper(s) deleted", Toast.LENGTH_SHORT).show();

        // Exit selection mode
        exitSelectionMode();

        // Refresh the list
        sortAndUpdateList();

        // TODO: Delete from database via ViewModel
        // viewModel.deletePapersFromReadingList(readingListId, selectedPaperIds);
    }

    /**
     * Load mock test data for visual testing of sort functionality
     * This creates diverse paper items with different titles, years, and statuses
     */
    private void loadMockTestData() {
        currentPaperItems.clear();

        // Add diverse test papers to demonstrate sorting
        currentPaperItems.add(new PaperAdapter.PaperItem(
                "Transformer-based Models for Natural Language Processing",
                "J. Smith, A. Johnson, and M. Williams",
                "Unread",
                2023,
                R.drawable.ic_paper_placeholder
        ));

        currentPaperItems.add(new PaperAdapter.PaperItem(
                "Deep Learning Techniques for Image Recognition",
                "E. Brown, C. Davis, and S. Green",
                "Reading",
                2022,
                R.drawable.ic_paper_placeholder
        ));

        currentPaperItems.add(new PaperAdapter.PaperItem(
                "Reinforcement Learning for Robotics",
                "R. Taylor, L. Clark, and P. White",
                "Finished",
                2021,
                R.drawable.ic_paper_placeholder
        ));

        currentPaperItems.add(new PaperAdapter.PaperItem(
                "AI Ethics and Responsible Machine Learning",
                "K. Martinez, J. Anderson, and H. Lee",
                "Unread",
                2024,
                R.drawable.ic_paper_placeholder
        ));

        currentPaperItems.add(new PaperAdapter.PaperItem(
                "Convolutional Neural Networks: A Survey",
                "M. Wilson, T. Moore, and D. Jackson",
                "Reading",
                2020,
                R.drawable.ic_paper_placeholder
        ));

        currentPaperItems.add(new PaperAdapter.PaperItem(
                "Graph Neural Networks for Social Network Analysis",
                "S. Thompson, B. Harris, and N. Martin",
                "Finished",
                2023,
                R.drawable.ic_paper_placeholder
        ));

        currentPaperItems.add(new PaperAdapter.PaperItem(
                "Attention Mechanisms in Neural Machine Translation",
                "D. Garcia, C. Rodriguez, and L. Hernandez",
                "Unread",
                2019,
                R.drawable.ic_paper_placeholder
        ));

        currentPaperItems.add(new PaperAdapter.PaperItem(
                "Federated Learning: Challenges and Opportunities",
                "A. Lopez, M. Gonzalez, and R. Perez",
                "Reading",
                2024,
                R.drawable.ic_paper_placeholder
        ));

        currentPaperItems.add(new PaperAdapter.PaperItem(
                "Zero-Shot Learning with Semantic Embeddings",
                "J. Young, K. King, and S. Wright",
                "Finished",
                2022,
                R.drawable.ic_paper_placeholder
        ));

        currentPaperItems.add(new PaperAdapter.PaperItem(
                "Explainable AI: Methods and Applications",
                "P. Scott, T. Green, and B. Adams",
                "Unread",
                2021,
                R.drawable.ic_paper_placeholder
        ));

        // Apply initial sort and update the list
        sortAndUpdateList();
    }
}

