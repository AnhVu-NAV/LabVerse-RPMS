package com.prm392.g5.labverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.prm392.g5.labverse.R;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Setup bottom navigation with the current selected item
     * Call this method after setContentView() in child activities
     * @param selectedItemId The menu item id to be selected (e.g., R.id.navigation_library)
     */
    protected void setupBottomNavigation(int selectedItemId) {
        bottomNavigation = findViewById(R.id.bottom_navigation);

        if (bottomNavigation == null) {
            return;
        }

        // Set the selected item
        bottomNavigation.setSelectedItemId(selectedItemId);

        // Set up navigation listener
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Avoid restarting the same activity
            if (itemId == selectedItemId) {
                return true;
            }

            if (itemId == R.id.navigation_library) {
                navigateToActivity(MyLibraryActivity.class);
                return true;
            } else if (itemId == R.id.navigation_reading_list) {
                // TODO: Navigate to Reading List when implemented
                Toast.makeText(this, "Reading List - Coming Soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_groups) {
                // TODO: Navigate to Groups when implemented
                Toast.makeText(this, "Groups - Coming Soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_explore) {
                navigateToActivity(SearchActivity.class);
                return true;
            }

            return false;
        });
    }

    /**
     * Navigate to another activity and finish current one to avoid activity stack buildup
     */
    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        // Disable transition animation for smoother navigation
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure correct item is selected when returning to this activity
        if (bottomNavigation != null && getSelectedNavigationItemId() != 0) {
            bottomNavigation.setSelectedItemId(getSelectedNavigationItemId());
        }
    }

    /**
     * Override this method in child activities to specify which navigation item should be selected
     * @return The menu item id (e.g., R.id.navigation_library)
     */
    protected abstract int getSelectedNavigationItemId();
}

