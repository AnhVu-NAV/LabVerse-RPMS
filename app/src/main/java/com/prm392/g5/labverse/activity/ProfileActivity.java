package com.prm392.g5.labverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.viewmodel.UserViewModel;

public class ProfileActivity extends AppCompatActivity {

    private UserViewModel viewModel;
    private TextView tvFullName, tvEmail, tvPhone, tvGender, tvAddress;
    private Button btnEditProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvFullName  = findViewById(R.id.tvFullName);
        tvEmail     = findViewById(R.id.tvEmail);
        tvPhone     = findViewById(R.id.tvPhone);
        tvGender    = findViewById(R.id.tvGender);
        tvAddress   = findViewById(R.id.tvAddress);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (d, w) -> {
                        // clear token & local cache
                        SharePreferenceManager.getInstance().clearUserAuthData();
                        AppDatabase.databaseWriteExecutor.execute(() ->
                                AppDatabase.getInstance(getApplicationContext()).userDao().clearUser()
                        );
                        Intent i = new Intent(this, LoginActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        viewModel.getUserProfile().observe(this, user -> {
            if (user != null) {
                tvFullName.setText(user.getFullName() == null ? "—" : user.getFullName());
                tvEmail.setText(user.getEmail() == null ? "—" : user.getEmail());
                tvPhone.setText(user.getPhoneNumber() == null ? "—" : user.getPhoneNumber());
                Boolean g = user.getGender(); // Boolean nullable
                tvGender.setText(g == null ? "—" : (g ? "Male" : "Female"));
                tvAddress.setText(user.getAddress() == null ? "—" : user.getAddress());
            }
        });

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh lại mỗi lần quay về màn này
        String userId = SharePreferenceManager.getInstance().getUserId();
        viewModel.refreshUser(userId);
    }
}
