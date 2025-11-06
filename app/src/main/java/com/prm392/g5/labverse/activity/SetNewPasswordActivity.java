package com.prm392.g5.labverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.prm392.g5.labverse.R;

public class SetNewPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilPassword, tilConfirm;
    private TextInputEditText edPassword, edConfirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_password);

        tilPassword = findViewById(R.id.tilPassword);
        tilConfirm  = findViewById(R.id.tilConfirm);
        edPassword  = findViewById(R.id.edPassword);
        edConfirm   = findViewById(R.id.edConfirm);
        MaterialButton btnUpdate = findViewById(R.id.btnUpdate);

        btnUpdate.setOnClickListener(v -> {
            clearErrors();

            String p1 = safeText(edPassword);
            String p2 = safeText(edConfirm);

            // validate
            if (p1.length() < 8) {
                tilPassword.setError(getString(R.string.err_password_short));
                return;
            }
            // simple strength check: at least letter + digit
            if (!p1.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
                tilPassword.setError(getString(R.string.err_password_weak));
                return;
            }
            if (!p1.equals(p2)) {
                tilConfirm.setError(getString(R.string.err_password_mismatch));
                return;
            }

            // TODO: call backend API to set new password with reset token
            // giả lập thành công:
            Toast.makeText(this, getString(R.string.msg_password_updated), Toast.LENGTH_SHORT).show();

            // quay về Login
            startActivity(new Intent(this, PasswordSuccessActivity.class));
            finish();
        });
    }

    private void clearErrors() {
        tilPassword.setError(null);
        tilConfirm.setError(null);
    }

    private String safeText(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
