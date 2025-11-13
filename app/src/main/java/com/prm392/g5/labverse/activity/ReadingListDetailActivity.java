package com.prm392.g5.labverse.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.PaperAdapter;
import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.entity.ReadingListItem;
import com.prm392.g5.labverse.viewmodel.ReadingListDetailViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReadingListDetailActivity extends BaseActivity {

    public static final String EXTRA_READING_LIST_ID = "reading_list_id";
    public static final String EXTRA_READING_LIST_NAME = "reading_list_name";
    private static final int REQ_ADD_EXISTING_PAPERS = 3001;
    // Activity con sẽ trả về danh sách id (String hoặc Long tuỳ bạn) qua key này
    public static final String RESULT_SELECTED_PAPER_IDS = "result_selected_paper_ids";

    private ReadingListDetailViewModel viewModel;
    private PaperAdapter adapter;
    private RecyclerView recyclerView;
    private MaterialToolbar toolbar;
    private String readingListId;
    private String readingListName;

    private enum SortBy {
        CREATOR("Creator", "Sort By: Creator"),
        DATE("Date", "Sort By: Date"),
        DATE_ADDED("Date Added", "Sort By: Date Added"),
        PUBLICATION_TITLE("Publication Title", "Sort By: Publication Title"),
        TITLE("Title", "Sort By: Title"),
        YEAR("Year", "Sort By: Year");

        private final String displayName;
        SortBy(String n, String d) { this.displayName = d; }
        public String getDisplayName() { return displayName; }
    }
    private enum SortOrder { ASCENDING, DESCENDING }

    private SortBy currentSortBy = SortBy.TITLE;
    private SortOrder currentSortOrder = SortOrder.ASCENDING;

    private final List<PaperAdapter.PaperItem> currentPaperItems = new ArrayList<>();
    private boolean isSelectionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_list_detail);

        // Extras
        readingListId = getIntent().getStringExtra(EXTRA_READING_LIST_ID);
        readingListName = getIntent().getStringExtra(EXTRA_READING_LIST_NAME);

        if (readingListId == null || readingListId.trim().isEmpty()) {
            Toast.makeText(this, "Error: Invalid reading list", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Views
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.papers_recycler_view);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_paper);

        // Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(readingListName != null ? readingListName : getString(R.string.reading_list));
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // RecyclerView + Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaperAdapter(new ArrayList<>(), (paper, position) -> {
            if (isSelectionMode) {
                updateSelectionModeUI();
            } else {
                Toast.makeText(this, "Opening paper: " + paper.title, Toast.LENGTH_SHORT).show();
                // TODO: điều hướng sang PaperDetail
            }
        });
        adapter.setOnLongClickListener((paper, position) -> {
            if (!isSelectionMode) {
                enterSelectionMode();
                adapter.toggleSelection(position);
                updateSelectionModeUI();
            }
        });
        recyclerView.setAdapter(adapter);

        // Back press trong selection mode
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (isSelectionMode) exitSelectionMode();
                else { setEnabled(false); getOnBackPressedDispatcher().onBackPressed(); }
            }
        });

        // ViewModel
        viewModel = new ViewModelProvider(this).get(ReadingListDetailViewModel.class);

        // ViewModel phải có overload setReadingListId(String)
        // (mình đã gửi bạn hàm này ở reply trước)
        viewModel.setReadingListId(readingListId);

        viewModel.getPapersInReadingList().observe(this, new Observer<List<Paper>>() {
            @Override
            public void onChanged(List<Paper> papersList) {
                currentPaperItems.clear();
                if (papersList != null) {
                    for (Paper p : papersList) {
                        currentPaperItems.add(mapToItem(p));
                    }
                }
                sortAndUpdateList();
            }
        });
        adapter.setActionsListener((anchor, item, position) -> showPaperItemMenu(anchor, item, position));


        // FAB
        fabAdd.setOnClickListener(v -> showAddPaperDialog());

        // Bottom nav
        setupBottomNavigation(R.id.navigation_reading_list);
    }

    private void showPaperItemMenu(View anchor, PaperAdapter.PaperItem item, int position) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_paper_item, popup.getMenu());

        popup.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.action_open) {
                // “Edit/Open”: mở chi tiết (hoặc màn sửa khi bạn có)
                Toast.makeText(this, "Open: " + item.title, Toast.LENGTH_SHORT).show();
                // TODO: startActivity(PaperDetailActivity...)
                return true;
            } else if (id == R.id.action_remove) {
                // Hỏi xác nhận trước khi xóa khỏi list
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Remove from this list")
                        .setMessage("Remove \"" + item.title + "\" from \"" +
                                (readingListName != null ? readingListName : "Reading List") + "\"?")
                        .setPositiveButton("Remove", (d, w) -> {
                            String paperId = item.id;
                            viewModel.deletePaperFromReadingList(
                                    readingListId,
                                    paperId,
                                    () -> {
                                        Toast.makeText(this, "Removed", Toast.LENGTH_SHORT).show();
                                        viewModel.reloadPapersInReadingList(); // refresh UI
                                    },
                                    err -> Toast.makeText(this, "Remove failed: " + err, Toast.LENGTH_SHORT).show()
                            );
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        });

        popup.show();
    }


    // ===== Menu =====
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        menu.clear();
        if (isSelectionMode) getMenuInflater().inflate(R.menu.menu_selection_mode, menu);
        else getMenuInflater().inflate(R.menu.menu_reading_list_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        if (isSelectionMode) {
            int selectedCount = adapter != null ? adapter.getSelectedCount() : 0;
            android.view.MenuItem selectAllItem = menu.findItem(R.id.action_select_all);
            if (selectAllItem != null) {
                selectAllItem.setTitle((selectedCount > 0 && selectedCount == currentPaperItems.size())
                        ? R.string.deselect_all : R.string.select_all);
                selectAllItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS
                        | android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }
            android.view.MenuItem deleteItem = menu.findItem(R.id.action_delete);
            if (deleteItem != null) deleteItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        if (isSelectionMode) {
            if (item.getItemId() == R.id.action_select_all) {
                handleSelectAllToggle(); invalidateOptionsMenu(); return true;
            } else if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog(); return true;
            }
            return super.onOptionsItemSelected(item);
        }
        if (item.getItemId() == R.id.action_sort) { showSortDialog(); return true; }
        else if (item.getItemId() == R.id.action_more) { showMoreOptionsMenu(); return true; }
        return super.onOptionsItemSelected(item);
    }

    // ===== Mapping từ entity Room sang item UI =====
    private PaperAdapter.PaperItem mapToItem(com.prm392.g5.labverse.entity.Paper p) {
        String title   = safe(p.getTitle());
        String authors = safe(p.getAuthorName());
        String journal = safe(p.getJournalName());
        int total = Math.max(p.getTotalPage(), 1);
        int curr  = Math.max(0, Math.min(p.getCurrentPage(), total));
        int progress = (curr * 100) / total;
        String status = (curr == 0) ? "Unread" : (curr >= total ? "Finished" : "Reading");

        return new PaperAdapter.PaperItem(
                p.getId(),                         // 👈 CHỈNH: truyền id
                title, authors, journal, status, progress,
                R.drawable.ic_paper_placeholder
        );
    }



    private String safe(String s){ return s==null ? "" : s; }

    // ===== Sort & Update =====
    private void sortAndUpdateList() {
        List<PaperAdapter.PaperItem> sorted = new ArrayList<>(currentPaperItems);

        Comparator<PaperAdapter.PaperItem> cmp;
        switch (currentSortBy) {
            case CREATOR:           cmp = (a,b)->ci(a.authors).compareTo(ci(b.authors)); break;
            case DATE:              // chưa có field -> tạm theo title length
                cmp = Comparator.comparingInt(a -> a.title==null ? 0 : a.title.length()); break;
            case DATE_ADDED:        // cần field "addedAt" trong rlp, tạm giữ nguyên
                cmp = Comparator.comparingInt(a -> a.title==null ? 0 : a.title.length()); break;
            case PUBLICATION_TITLE: cmp = (a,b)->ci(a.status).compareTo(ci(b.status)); break;
            case TITLE:             cmp = (a,b)->ci(a.title).compareTo(ci(b.title)); break;
            case YEAR:              // chưa có year -> tạm theo progress
                cmp = Comparator.comparingInt(a -> a.progress); break;
            default:                cmp = (a,b)->0;
        }
        sorted.sort(cmp);
        if (currentSortOrder == SortOrder.DESCENDING) Collections.reverse(sorted);

        adapter.setItems(sorted);
    }

    private String ci(String s){ return s==null ? "" : s.toLowerCase(); }

    // ===== More / Sort dialogs, unchanged logic =====
    private void showMoreOptionsMenu() {
        View moreButton = toolbar.findViewById(R.id.action_more);
        if (moreButton == null) moreButton = toolbar;
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(this, moreButton);
        popupMenu.getMenuInflater().inflate(R.menu.menu_reading_list_more, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.action_delete_mode) { enterSelectionMode(); return true; }
            return false;
        });
        popupMenu.show();
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

        if (sortByRow != null) sortByRow.setOnClickListener(v -> showSortByCriteriaDialog(dialog));
        if (btnAscending != null)  btnAscending.setOnClickListener(v -> { currentSortOrder = SortOrder.ASCENDING;  updateSortOrderButtons(btnAscending, btnDescending); sortAndUpdateList(); });
        if (btnDescending != null) btnDescending.setOnClickListener(v -> { currentSortOrder = SortOrder.DESCENDING; updateSortOrderButtons(btnAscending, btnDescending); sortAndUpdateList(); });

        dialog.show();
    }

    private void showSortByCriteriaDialog(BottomSheetDialog parentDialog) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_sort_by);

        View btnBack = dialog.findViewById(R.id.btn_back);
        LinearLayout optCreator = dialog.findViewById(R.id.sort_option_creator);
        LinearLayout optDate = dialog.findViewById(R.id.sort_option_date);
        LinearLayout optDateAdded = dialog.findViewById(R.id.sort_option_date_added);
        LinearLayout optPubTitle = dialog.findViewById(R.id.sort_option_publication_title);
        LinearLayout optTitle = dialog.findViewById(R.id.sort_option_title);
        LinearLayout optYear = dialog.findViewById(R.id.sort_option_year);

        android.widget.ImageView checkCreator = dialog.findViewById(R.id.check_creator);
        android.widget.ImageView checkDate = dialog.findViewById(R.id.check_date);
        android.widget.ImageView checkDateAdded = dialog.findViewById(R.id.check_date_added);
        android.widget.ImageView checkPublicationTitle = dialog.findViewById(R.id.check_publication_title);
        android.widget.ImageView checkTitle = dialog.findViewById(R.id.check_title);
        android.widget.ImageView checkYear = dialog.findViewById(R.id.check_year);

        updateSortByCheckmarks(checkCreator, checkDate, checkDateAdded, checkPublicationTitle, checkTitle, checkYear);
        if (btnBack != null) btnBack.setOnClickListener(v -> { dialog.dismiss(); parentDialog.show(); });

        if (optCreator != null) optCreator.setOnClickListener(v -> { currentSortBy = SortBy.CREATOR; sortAndUpdateList(); dialog.dismiss(); updateParentDialogSortByLabel(parentDialog); parentDialog.show(); });
        if (optDate != null) optDate.setOnClickListener(v -> { currentSortBy = SortBy.DATE; sortAndUpdateList(); dialog.dismiss(); updateParentDialogSortByLabel(parentDialog); parentDialog.show(); });
        if (optDateAdded != null) optDateAdded.setOnClickListener(v -> { currentSortBy = SortBy.DATE_ADDED; sortAndUpdateList(); dialog.dismiss(); updateParentDialogSortByLabel(parentDialog); parentDialog.show(); });
        if (optPubTitle != null) optPubTitle.setOnClickListener(v -> { currentSortBy = SortBy.PUBLICATION_TITLE; sortAndUpdateList(); dialog.dismiss(); updateParentDialogSortByLabel(parentDialog); parentDialog.show(); });
        if (optTitle != null) optTitle.setOnClickListener(v -> { currentSortBy = SortBy.TITLE; sortAndUpdateList(); dialog.dismiss(); updateParentDialogSortByLabel(parentDialog); parentDialog.show(); });
        if (optYear != null) optYear.setOnClickListener(v -> { currentSortBy = SortBy.YEAR; sortAndUpdateList(); dialog.dismiss(); updateParentDialogSortByLabel(parentDialog); parentDialog.show(); });

        dialog.show();
    }

    private void updateSortByCheckmarks(android.widget.ImageView checkCreator,
                                        android.widget.ImageView checkDate,
                                        android.widget.ImageView checkDateAdded,
                                        android.widget.ImageView checkPublicationTitle,
                                        android.widget.ImageView checkTitle,
                                        android.widget.ImageView checkYear) {
        if (checkCreator != null) checkCreator.setVisibility(View.GONE);
        if (checkDate != null) checkDate.setVisibility(View.GONE);
        if (checkDateAdded != null) checkDateAdded.setVisibility(View.GONE);
        if (checkPublicationTitle != null) checkPublicationTitle.setVisibility(View.GONE);
        if (checkTitle != null) checkTitle.setVisibility(View.GONE);
        if (checkYear != null) checkYear.setVisibility(View.GONE);

        switch (currentSortBy) {
            case CREATOR:           if (checkCreator != null) checkCreator.setVisibility(View.VISIBLE); break;
            case DATE:              if (checkDate != null) checkDate.setVisibility(View.VISIBLE); break;
            case DATE_ADDED:        if (checkDateAdded != null) checkDateAdded.setVisibility(View.VISIBLE); break;
            case PUBLICATION_TITLE: if (checkPublicationTitle != null) checkPublicationTitle.setVisibility(View.VISIBLE); break;
            case TITLE:             if (checkTitle != null) checkTitle.setVisibility(View.VISIBLE); break;
            case YEAR:              if (checkYear != null) checkYear.setVisibility(View.VISIBLE); break;
        }
    }

    private void updateParentDialogSortByLabel(BottomSheetDialog parentDialog) {
        View parentView = parentDialog.findViewById(R.id.sort_by_label);
        if (parentView instanceof TextView) ((TextView) parentView).setText(currentSortBy.getDisplayName());
    }

    private void updateSortOrderButtons(MaterialButton btnAscending, MaterialButton btnDescending) {
        if (currentSortOrder == SortOrder.ASCENDING) {
            btnAscending.setSelected(true); btnDescending.setSelected(false);
        } else {
            btnAscending.setSelected(false); btnDescending.setSelected(true);
        }
    }

    @Override
    protected int getSelectedNavigationItemId() { return R.id.navigation_reading_list; }

    // ===== Selection mode =====
    private void enterSelectionMode() {
        isSelectionMode = true; adapter.setSelectionMode(true);
        updateSelectionModeUI(); invalidateOptionsMenu();
    }
    private void exitSelectionMode() {
        isSelectionMode = false; adapter.setSelectionMode(false);
        updateNormalModeUI(); invalidateOptionsMenu();
    }
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
    private void updateNormalModeUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(readingListName != null ? readingListName : getString(R.string.reading_list));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        invalidateOptionsMenu();
    }
    private void handleSelectAllToggle() {
        int selectedCount = adapter.getSelectedCount();
        if (selectedCount == currentPaperItems.size() && selectedCount > 0) adapter.deselectAll();
        else adapter.selectAll();
        updateSelectionModeUI();
    }

    private void showDeleteConfirmationDialog() {
        int selectedCount = adapter.getSelectedCount();
        if (selectedCount == 0) { Toast.makeText(this, "No papers selected", Toast.LENGTH_SHORT).show(); return; }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_deletion, null);
        TextView messageText = dialogView.findViewById(R.id.dialog_message);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnDelete = dialogView.findViewById(R.id.btn_delete);

        messageText.setText("Are you sure you want to delete " + selectedCount + " selected paper" + (selectedCount > 1 ? "s" : "") + "?");

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView).setCancelable(true).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> { deleteSelectedPapers(); dialog.dismiss(); });
        dialog.show();
    }

    void showRenameDialog(String listId, String currentName) {
        final var edit = new com.google.android.material.textfield.TextInputEditText(this);
        edit.setText(currentName);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Rename")
                .setView(edit)
                .setPositiveButton("Save", (d, w) -> {
                    String newName = String.valueOf(edit.getText());
                    viewModel.renameReadingList(listId, newName); // gọi API rồi update Room
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSelectedPapers() {
        List<PaperAdapter.PaperItem> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) {
            Toast.makeText(this, "No papers selected", Toast.LENGTH_SHORT).show();
            return;
        }

        final int total = selected.size();
        final int[] done = {0};
        final int[] fail = {0};

        for (PaperAdapter.PaperItem it : selected) {
            viewModel.deletePaperFromReadingList(
                    readingListId,
                    it.id, // 👈 dùng id thật
                    () -> {
                        done[0]++;
                        if (done[0] + fail[0] == total) {
                            Toast.makeText(this,
                                    "Deleted " + done[0] + "/" + total + " paper(s)",
                                    Toast.LENGTH_SHORT).show();
                            exitSelectionMode();
                        }
                    },
                    err -> {
                        fail[0]++;
                        if (done[0] + fail[0] == total) {
                            Toast.makeText(this,
                                    "Deleted " + done[0] + "/" + total + " paper(s) (some failed)",
                                    Toast.LENGTH_SHORT).show();
                            exitSelectionMode();
                        }
                    }
            );
        }
    }

    private void showReadingListMenu(View anchor, ReadingListItem item, int position) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_reading_list_item, popup.getMenu());

        popup.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.action_edit) {
                // Sử dụng hàm bạn đã có:
                showRenameDialog(item.getId(), item.getPaper().getTitle());
                return true;
            } else if (id == R.id.action_delete) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Delete list")
                        .setMessage("Delete \"" + item.getPaper().getTitle() + "\"? This action cannot be undone.")
                        .setPositiveButton("Delete", (d, w) ->
                                viewModel.deleteList(item.getId(), () -> {
                                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                    // reload lists
                                })
                        )
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        });

        popup.show();
    }


    private void showAddPaperDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_add_paper);

        MaterialButton importFileButton = dialog.findViewById(R.id.import_file_button);
        MaterialButton addPaperButton   = dialog.findViewById(R.id.add_paper_button);

        if (importFileButton != null) {
            importFileButton.setOnClickListener(v -> {
                dialog.dismiss();
                ImportPaperActivity.open(this);
            });
        }

        if (addPaperButton != null) {
            addPaperButton.setOnClickListener(v -> {
                // MỞ màn chọn paper có sẵn
                android.content.Intent intent = new android.content.Intent(this, AddPapersToReadingListActivity.class);
                intent.putExtra(AddPapersToReadingListActivity.EXTRA_READING_LIST_ID, readingListId);
                intent.putExtra(AddPapersToReadingListActivity.EXTRA_READING_LIST_NAME, readingListName);
                startActivityForResult(intent, REQ_ADD_EXISTING_PAPERS);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ADD_EXISTING_PAPERS && resultCode == RESULT_OK && data != null) {
            // Nhận danh sách id paper đã chọn
            ArrayList<String> selectedIds = data.getStringArrayListExtra(RESULT_SELECTED_PAPER_IDS);
            if (selectedIds == null || selectedIds.isEmpty()) {
                Toast.makeText(this, "No papers selected", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi API addExisting cho từng paper
            // Gợi ý: ViewModel nên có hàm addExistingPaper(listId, paperId, onDone)
            final int total = selectedIds.size();
            final int[] done = {0};
            for (String paperId : selectedIds) {
                viewModel.addExistingPaper(
                        String.valueOf(readingListId), // nếu BE dùng String id, còn VM của bạn nhận long thì truyền long
                        paperId,
                        () -> {
                            done[0]++;
                            if (done[0] == total) {
                                Toast.makeText(this, "Added " + total + " paper(s)", Toast.LENGTH_SHORT).show();
                                // reload danh sách (VM của bạn có thể tự reload trong addExistingPaper; nếu không thì gọi thủ công)
                                viewModel.reloadPapersInReadingList();
                            }
                        },
                        errMsg -> {
                            // Thông báo lỗi từng phần nhưng không chặn các item còn lại
                            Toast.makeText(this, "Failed: " + errMsg, Toast.LENGTH_SHORT).show();
                        }
                );
            }
        }
    }

}
