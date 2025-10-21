package com.prm392.g5.labverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.adapter.PaperAdapter;
import java.util.ArrayList;
import java.util.List;

public class MyLibraryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_library);

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        RecyclerView papersRecyclerView = findViewById(R.id.papers_recycler_view);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        // Set the toolbar as the action bar
        setSupportActionBar(toolbar);
        // Handle search icon click
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(MyLibraryActivity.this, SearchActivity.class);
            startActivity(intent);
        });


        // Setup RecyclerView with LinearLayoutManager
        papersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create sample data
        List<PaperAdapter.PaperItem> papers = new ArrayList<>();
        papers.add(new PaperAdapter.PaperItem(
            "The Impact of AI on Education",
            "Dr. Emily Carter, Dr. David Lee",
            "Unread",
            0,
            0
        ));
        papers.add(new PaperAdapter.PaperItem(
            "Sustainable Energy Solutions",
            "Dr. Maria Rodriguez, Dr. John Smith",
            "Reading",
            60,
            0
        ));
        papers.add(new PaperAdapter.PaperItem(
            "Advancements in Biotechnology",
            "Dr. Robert Johnson, Dr. Sarah Williams",
            "Finished",
            100,
            0
        ));

        // Set adapter
        PaperAdapter adapter = new PaperAdapter(papers);
        papersRecyclerView.setAdapter(adapter);

        // Set the default selected item for the bottom navigation
        bottomNavigation.setSelectedItemId(R.id.navigation_library);

        // Set click listener for FAB
        fabAdd.setOnClickListener(v -> {
            // TODO: Implement add paper functionality
            // For now, just show a toast or log
        });

        // Further UI setup and adapter implementation would go here
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_library_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            // TODO: Handle profile click
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            // Handle navigation icon (search) click
            // TODO: Implement search functionality
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
