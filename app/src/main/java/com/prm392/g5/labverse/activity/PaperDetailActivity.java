package com.prm392.g5.labverse.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.PaperDetailPagerAdapter;

public class PaperDetailActivity extends BaseActivity {
    public static final String EXTRA_PAPER_ID = "paper_id";
    public static final String EXTRA_PAPER_TITLE = "paper_title";
    public static final String EXTRA_PAPER_AUTHORS = "paper_authors";
    public static final String EXTRA_PAPER_STATUS = "paper_status";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Toolbar toolbar;
    private String paperTitle;
    private String paperId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_detail);

        // Get data from Intent
        Intent intent = getIntent();
        paperTitle = intent.getStringExtra(EXTRA_PAPER_TITLE);
        paperId = intent.getStringExtra(EXTRA_PAPER_ID);
        if (paperTitle == null) {
            paperTitle = "The Impact of AI on Education"; // Default title
        }

        initViews();
        setupToolbar();
        setupViewPager();
        setupBottomNavigation(R.id.navigation_library);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(paperTitle);
        }

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupViewPager() {
        PaperDetailPagerAdapter adapter = new PaperDetailPagerAdapter(this, paperId);
        viewPager.setAdapter(adapter);

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Read");
                    break;
                case 1:
                    tab.setText("Citation");
                    break;
                case 2:
                    tab.setText("Annotations");
                    break;
                case 3:
                    tab.setText("Discussion");
                    break;
            }
        }).attach();
    }

    public static void open(
            Context context,
            String paperId,
            String paperTitle,
            @Nullable String teamId,
            @Nullable String readingListId,
            boolean isTeamContext
    ) {
        Intent intent = new Intent(context, PaperDetailActivity.class);
        intent.putExtra(EXTRA_PAPER_ID, paperId);
        intent.putExtra(EXTRA_PAPER_TITLE, paperTitle);
        if (teamId != null) intent.putExtra("TEAM_ID", teamId);
        if (readingListId != null) intent.putExtra("READING_LIST_ID", readingListId);
        intent.putExtra("IS_TEAM_CONTEXT", isTeamContext);
        context.startActivity(intent);
    }


    @Override
    protected int getSelectedNavigationItemId() {
        return R.id.navigation_library;
    }
}

