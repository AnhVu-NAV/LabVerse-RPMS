package com.prm392.g5.labverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.PaperAdapter;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.entity.PaperCache;
import com.prm392.g5.labverse.util.WorkScheduler;
import com.prm392.g5.labverse.viewmodel.MyLibraryViewModel;

import java.util.ArrayList;
import java.util.List;

public class MyLibraryActivity extends BaseActivity {

    private MyLibraryViewModel vm;
    private PaperAdapter adapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_library);

        // Views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        RecyclerView papersRecyclerView = findViewById(R.id.papers_recycler_view);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);

        // Toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(MyLibraryActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // RecyclerView + Adapter rỗng (dữ liệu sẽ đổ từ LiveData)
        papersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaperAdapter(new ArrayList<>(), (paper, position) -> {
            Intent intent = new Intent(MyLibraryActivity.this, PaperDetailActivity.class);
            intent.putExtra(PaperDetailActivity.EXTRA_PAPER_TITLE, paper.title);
            intent.putExtra(PaperDetailActivity.EXTRA_PAPER_AUTHORS, paper.authors);
            intent.putExtra(PaperDetailActivity.EXTRA_PAPER_STATUS, paper.status);
            startActivity(intent);
        });
        papersRecyclerView.setAdapter(adapter);

        // Bottom nav
        setupBottomNavigation(R.id.navigation_library);

        // FAB (Import New Paper)
        fabAdd.setOnClickListener(v -> {
            // TODO: mở màn import hoặc gọi API lấy uploadUrl rồi chuyển màn
        });

        // ViewModel
        vm = new ViewModelProvider(this).get(MyLibraryViewModel.class);

        String userId = SharePreferenceManager.getInstance().getUserId();
        if (userId == null) {
            // user chưa login → đẩy về Login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        vm.initUser(userId);
        WorkScheduler.initSync(getApplicationContext());
//        WorkScheduler.prefetchRecent(getApplicationContext());

        // observe
        vm.getPapers().observe(this, list -> {
            List<PaperAdapter.PaperItem> items = new ArrayList<>();
            for (PaperCache e : list) {
                items.add(new PaperAdapter.PaperItem(
                        e.title,
                        e.authors,
                        e.journal,
                        toUiStatus(e.status),
                        e.progress,
                        R.drawable.ic_paper_placeholder
                ));
            }
            adapter.setItems(items);
        });


        // Tabs → đổi filter + trigger sync
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: vm.setFilter("recently_added"); break;
                    case 1: vm.setFilter("recently_read");  break;
                    case 2: vm.setFilter("favorites");      break;
                }
                WorkScheduler.initSync(getApplicationContext());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Lần đầu: Recently Added + sync
        vm.setFilter("recently_added");
        // (tuỳ chọn) chọn đúng tab đầu tiên trên UI nếu cần
        if (tabLayout.getTabAt(0) != null) tabLayout.getTabAt(0).select();
    }

    // Map từ cache entity → item hiển thị
    private PaperAdapter.PaperItem map(PaperCache e) {
        return new PaperAdapter.PaperItem(
                e.title,
                e.authors,
                e.journal,
                toUiStatus(e.status),
                e.progress,
                0 /* commentsCount nếu có */
        );
    }

    private String toUiStatus(String s){
        if (s == null) return "Unread";
        switch (s) {
            case "READING":  return "Reading";
            case "FINISHED": return "Finished";
            default:         return "Unread";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_library_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            // TODO: mở profile
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            // icon search trên toolbar (đã handle ở setNavigationOnClickListener)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelectedNavigationItemId() {
        return R.id.navigation_library;
    }
}
