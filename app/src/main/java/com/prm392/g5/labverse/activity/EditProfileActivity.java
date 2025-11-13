package com.prm392.g5.labverse.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.entity.UserEntity;
import com.prm392.g5.labverse.viewmodel.UserViewModel;

public class EditProfileActivity extends AppCompatActivity {
    private UserViewModel viewModel;
    private TextInputEditText edName, edPhone, edAddress;
    private SwitchMaterial swGender;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ImageView imgAvatar = findViewById(R.id.imgAvatar);
        edName    = findViewById(R.id.edName);
        edPhone   = findViewById(R.id.edPhone);
        edAddress = findViewById(R.id.edAddress);
        swGender  = findViewById(R.id.swGender);
        btnSave   = findViewById(R.id.btnSave);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Prefill dữ liệu hiện tại
        viewModel.getUserProfile().observe(this, user -> {
            if (user != null) {
                edName.setText(user.getFullName());
                edPhone.setText(user.getPhoneNumber());
                edAddress.setText(user.getAddress());
                Boolean g = user.getGender(); // nullable
                swGender.setChecked(Boolean.TRUE.equals(g)); // true=Male, false/null=Female
            }
        });

        imgAvatar.setOnClickListener(v ->
                Toast.makeText(this, "Feature coming soon: change avatar", Toast.LENGTH_SHORT).show()
        );

        btnSave.setOnClickListener(v -> {
            UserEntity updated = new UserEntity();
            updated.setId(SharePreferenceManager.getInstance().getUserId());
            updated.setFullName(edName.getText() == null ? null : edName.getText().toString());
            updated.setPhoneNumber(edPhone.getText() == null ? null : edPhone.getText().toString());
            updated.setAddress(edAddress.getText() == null ? null : edAddress.getText().toString());
            // boolean mới
            updated.setGender(swGender.isChecked()); // true = Male, false = Female

            viewModel.updateUser(updated);
            Toast.makeText(this, "Profile updated (syncs when online)", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
