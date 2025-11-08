package com.prm392.g5.labverse.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.ReadingListAdapter;
import com.prm392.g5.labverse.entity.ReadingList;
import com.prm392.g5.labverse.viewmodel.ReadingListViewModel;

import java.util.ArrayList;
import java.util.List;

public class ReadingListActivity extends BaseActivity {

    private ReadingListViewModel viewModel;
    private ReadingListAdapter adapter;
    private RecyclerView recyclerView;
    private MaterialToolbar toolbar;

    // Sort options
    private enum SortBy {
        NAME("Sort By: Name"),
        DATE("Sort By: Date Created");

        private final String displayName;

        SortBy(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private enum SortOrder {
        ASCENDING, DESCENDING
    }

    private SortBy currentSortBy = SortBy.NAME;
    private SortOrder currentSortOrder = SortOrder.ASCENDING;
    private List<ReadingList> currentReadingLists = new ArrayList<>();
    private boolean isSelectionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_list);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.reading_lists_recycler_view);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_reading_list);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReadingListAdapter((readingList, position) -> {
            if (isSelectionMode) {
                updateSelectionModeUI();
            } else {
                // Navigate to Reading List Detail
                android.content.Intent intent = new android.content.Intent(this, ReadingListDetailActivity.class);
                intent.putExtra(ReadingListDetailActivity.EXTRA_READING_LIST_ID, readingList.getId());
                intent.putExtra(ReadingListDetailActivity.EXTRA_READING_LIST_NAME, readingList.getName());
                startActivity(intent);
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
        viewModel = new ViewModelProvider(this).get(ReadingListViewModel.class);
        viewModel.getAllReadingLists().observe(this, readingLists -> {
            if (readingLists != null) {
                currentReadingLists = new ArrayList<>(readingLists);
                sortAndUpdateList();
            }
        });

        // Set up FAB click listener
        fabAdd.setOnClickListener(v -> showCreateReadingListDialog());

        // Set up bottom navigation
        setupBottomNavigation(R.id.navigation_reading_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isSelectionMode) {
            // Don't show normal menu in selection mode
            return true;
        }
        getMenuInflater().inflate(R.menu.reading_list_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (itemId == R.id.action_more) {
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

    private void showCreateReadingListDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_create_reading_list);

        TextInputEditText nameEditText = dialog.findViewById(R.id.name_edit_text);
        TextInputEditText descriptionEditText = dialog.findViewById(R.id.description_edit_text);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancel_button);
        MaterialButton createButton = dialog.findViewById(R.id.create_button);

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        if (createButton != null) {
            createButton.setOnClickListener(v -> {
                String name = nameEditText != null && nameEditText.getText() != null ? nameEditText.getText().toString().trim() : "";
                String description = descriptionEditText != null && descriptionEditText.getText() != null ? descriptionEditText.getText().toString().trim() : "";

                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter a reading list name", Toast.LENGTH_SHORT).show();
                    return;
                }

                ReadingList readingList = new ReadingList();
                readingList.setName(name);
                readingList.setDescription(description);

                viewModel.insert(readingList);
                Toast.makeText(this, "Reading list created", Toast.LENGTH_SHORT).show();
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

        if (sortByLabel != null) sortByLabel.setText(currentSortBy.getDisplayName());
        if (btnAscending != null && btnDescending != null) updateSortOrderButtons(btnAscending, btnDescending);

        if (sortByRow != null) {
            sortByRow.setOnClickListener(v -> {
                if (currentSortBy == SortBy.NAME) {
                    currentSortBy = SortBy.DATE;
                } else {
                    currentSortBy = SortBy.NAME;
                }
                if (sortByLabel != null) sortByLabel.setText(currentSortBy.getDisplayName());
                sortAndUpdateList();
            });
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
        if (currentReadingLists.isEmpty()) {
            return;
        }

        List<ReadingList> sortedLists = new ArrayList<>(currentReadingLists);

        if (currentSortBy == SortBy.NAME) {
            sortedLists.sort((o1, o2) -> {
                int result = o1.getName().compareToIgnoreCase(o2.getName());
                return currentSortOrder == SortOrder.ASCENDING ? result : -result;
            });
        } else {
            sortedLists.sort((o1, o2) -> {
                int result = Long.compare(o1.getId(), o2.getId());
                return currentSortOrder == SortOrder.ASCENDING ? result : -result;
            });
        }

        adapter.setReadingLists(sortedLists);
    }

    /**
     * Enter selection mode - allows multi-select of reading lists
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

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(selectedCount + " Selected");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        // Clear existing menu items and add selection mode items
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_selection_mode);

        // Set up close/cancel icon
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(v -> exitSelectionMode());

        // Handle menu item clicks
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_select_all) {
                handleSelectAllToggle();
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog();
                return true;
            }
            return false;
        });

        // Update Select All menu item text and style
        android.view.MenuItem selectAllItem = toolbar.getMenu().findItem(R.id.action_select_all);
        if (selectAllItem != null) {
            if (selectedCount == currentReadingLists.size() && selectedCount > 0) {
                selectAllItem.setTitle(R.string.deselect_all);
            } else {
                selectAllItem.setTitle(R.string.select_all);
            }
            // Force the menu item to show as text
            selectAllItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS | android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }

        // Ensure delete icon is visible
        android.view.MenuItem deleteItem = toolbar.getMenu().findItem(R.id.action_delete);
        if (deleteItem != null) {
            deleteItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    /**
     * Update toolbar UI for normal mode
     */
    private void updateNormalModeUI() {
        // Restore toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Reading Lists");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.reading_list_toolbar_menu);
        toolbar.setNavigationIcon(null);
        toolbar.setNavigationOnClickListener(null);

        // Restore menu item click listener
        toolbar.setOnMenuItemClickListener(null);
    }

    /**
     * Toggle between Select All and Deselect All
     */
    private void handleSelectAllToggle() {
        int selectedCount = adapter.getSelectedCount();
        if (selectedCount == currentReadingLists.size() && selectedCount > 0) {
            adapter.deselectAll();
        } else {
            adapter.selectAll();
        }
        updateSelectionModeUI();
    }

    /**
     * Show confirmation dialog before deleting selected reading lists
     */
    private void showDeleteConfirmationDialog() {
        int selectedCount = adapter.getSelectedCount();

        if (selectedCount == 0) {
            Toast.makeText(this, "No reading lists selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_deletion, null);
        TextView messageText = dialogView.findViewById(R.id.dialog_message);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnDelete = dialogView.findViewById(R.id.btn_delete);

        // Set custom message
        String message = "Are you sure you want to delete " + selectedCount + " selected reading list" + (selectedCount > 1 ? "s" : "") + "?";
        messageText.setText(message);

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
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
            deleteSelectedReadingLists();
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Delete selected reading lists from the database
     */
    private void deleteSelectedReadingLists() {
        int selectedCount = adapter.getSelectedCount();

        // Get selected items
        List<ReadingList> selectedItems = adapter.getSelectedItems();

        // Delete from database
        for (ReadingList readingList : selectedItems) {
            viewModel.delete(readingList);
        }

        // Remove from current list
        currentReadingLists.removeAll(selectedItems);

        // Update adapter
        adapter.removeSelectedItems();

        // Show confirmation
        Toast.makeText(this, selectedCount + " reading list(s) deleted", Toast.LENGTH_SHORT).show();

        // Exit selection mode
        exitSelectionMode();
    }

    @Override
    protected int getSelectedNavigationItemId() {
        return R.id.navigation_reading_list;
    }
}

