package com.prm392.g5.labverse.activity;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.prm392.g5.labverse.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ImageButton btnBack = findViewById(R.id.btnBack);
        tilEmail = findViewById(R.id.tilEmail);
        MaterialButton btnReset = findViewById(R.id.btnReset);

        btnBack.setOnClickListener(v -> finish());

        btnReset.setOnClickListener(v -> {
            String email = tilEmail.getEditText() == null ? "" : tilEmail.getEditText().getText().toString().trim();
            tilEmail.setError(null);

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.setError(getString(R.string.hint_email));
                return;
            }

            // TODO: gọi API reset password (send email). Demo:
            Toast.makeText(this, "Reset link sent to " + email, Toast.LENGTH_SHORT).show();
            finish(); // quay lại Login
        });
    }
}
