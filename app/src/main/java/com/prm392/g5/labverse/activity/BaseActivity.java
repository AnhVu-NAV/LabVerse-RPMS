package com.prm392.g5.labverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.activity.team.ListMyTeamsActivity;
import com.prm392.g5.labverse.activity.team.ListTeamOfPiActivity;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.util.NetworkStatus;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigation;
    private TextView tvNetworkBanner; // Hiển thị "Offline" / "Online"

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Quan sát trạng thái mạng
        NetworkStatus networkStatus = new NetworkStatus(getApplicationContext());
        networkStatus.observe(this, isConnected -> {
            if (isConnected != null) {
                updateNetworkBanner(isConnected);
            }
        });
    }

    /**
     * Gắn BottomNavigation và NetworkBanner (nếu layout có)
     */
    protected void setupBottomNavigation(int selectedItemId) {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        tvNetworkBanner = findViewById(R.id.tv_network_status); // TextView banner (tuỳ layout)

        if (bottomNavigation == null) {
            return;
        }

        bottomNavigation.setSelectedItemId(selectedItemId);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == selectedItemId) return true;

            if (itemId == R.id.navigation_library) {
                navigateToActivity(MyLibraryActivity.class);
                return true;
            } else if (itemId == R.id.navigation_reading_list) {
                navigateToActivity(ReadingListActivity.class);
                return true;
            } else if (itemId == R.id.navigation_groups) {
                String role = SharePreferenceManager.getInstance().getUserRole();
                if ("PI".equals(role)){
                    navigateToActivity(ListTeamOfPiActivity.class);
                }else {
                    navigateToActivity(ListMyTeamsActivity.class);
                }
                return true;
            } else if (itemId == R.id.navigation_explore) {
                navigateToActivity(SearchActivity.class);
                return true;
            }

            return false;
        });
    }

    private void updateNetworkBanner(boolean isOnline) {
        if (tvNetworkBanner == null) return;

        if (isOnline) {
            tvNetworkBanner.setText("🔵 Online");
            tvNetworkBanner.setBackgroundColor(getColor(R.color.green_600));
            tvNetworkBanner.setVisibility(View.VISIBLE);

            // Ẩn sau 2s cho nhẹ nhàng
            tvNetworkBanner.postDelayed(() -> tvNetworkBanner.setVisibility(View.GONE), 2000);
        } else {
            tvNetworkBanner.setText("⚪ Offline mode");
            tvNetworkBanner.setBackgroundColor(getColor(R.color.gray_700));
            tvNetworkBanner.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null && getSelectedNavigationItemId() != 0) {
            bottomNavigation.setSelectedItemId(getSelectedNavigationItemId());
        }
    }

    protected abstract int getSelectedNavigationItemId();
}
