package com.prm392.g5.labverse.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.PaperDetailPagerAdapter;

public class PaperDetailActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_detail);

        initViews();
        setupToolbar();
        setupViewPager();
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
            getSupportActionBar().setTitle("The Impact of AI on Education");
        }

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupViewPager() {
        PaperDetailPagerAdapter adapter = new PaperDetailPagerAdapter(this);
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
}

