package com.prm392.g5.labverse.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.prm392.g5.labverse.R;

public class PasswordResetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        MaterialButton btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> {
            // TODO: nếu luồng của bạn là về Login thì đổi class ở đây
            Intent i = new Intent(this, SetNewPasswordActivity.class);
            startActivity(i);
            finish();
        });
    }
}
