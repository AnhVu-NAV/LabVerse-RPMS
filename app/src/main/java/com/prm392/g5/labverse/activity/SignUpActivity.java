package com.prm392.g5.labverse.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.prm392.g5.labverse.R;

import java.util.Calendar;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {

    // TextInputLayout
    private TextInputLayout tilFullname, tilEmail, tilDob, tilPassword, tilConfirmPassword;
    // EditText
    private TextInputEditText edFullname, edEmail, edDob, edPassword, edConfirmPassword;
    // Buttons / actions
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        bindViews();
        setupDobPicker();
        setupLiveErrorClearing();
        setupActions();
    }

    private void bindViews() {
        tilFullname = findViewById(R.id.tilFullname);
        tilEmail = findViewById(R.id.tilEmail);
        tilDob = findViewById(R.id.tilDob);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        edFullname = findViewById(R.id.edFullname);
        edEmail = findViewById(R.id.edEmail);
        edDob = findViewById(R.id.edDob);
        edPassword = findViewById(R.id.edPassword);
        edConfirmPassword = findViewById(R.id.edConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);

        // "Login" quay lại màn trước
        findViewById(R.id.tvLogin).setOnClickListener(v -> finish());
    }

    private void setupDobPicker() {
        // Không bật bàn phím cho DOB
        edDob.setFocusable(false);
        edDob.setClickable(true);

        View.OnClickListener openDatePicker = v -> {
            final Calendar c = Calendar.getInstance();
            int y = c.get(Calendar.YEAR);
            int m = c.get(Calendar.MONTH);
            int d = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    SignUpActivity.this,
                    (view, yy, mm, dd) -> {
                        String formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", dd, (mm + 1), yy);
                        edDob.setText(formatted);
                        tilDob.setError(null);
                    },
                    y, m, d
            );
            dialog.show();
        };

        tilDob.setEndIconOnClickListener(openDatePicker);
        edDob.setOnClickListener(openDatePicker);
    }

    private void setupLiveErrorClearing() {
        // mỗi khi gõ lại thì clear error
        addTextChangedClearError(tilFullname, edFullname);
        addTextChangedClearError(tilEmail, edEmail);
        addTextChangedClearError(tilPassword, edPassword);
        addTextChangedClearError(tilConfirmPassword, edConfirmPassword);
    }

    private void addTextChangedClearError(TextInputLayout til, TextInputEditText et) {
        et.addTextChangedListener(new SimpleTextWatcher(() -> til.setError(null)));
    }

    private void setupActions() {
        btnRegister.setOnClickListener(v -> {
            hideKeyboard(v);
            if (!validateForm()) return;

            // TODO: gọi API đăng ký ở đây
            Toast.makeText(this, "Sign up OK ✅", Toast.LENGTH_SHORT).show();
            // Ví dụ: finish để quay lại màn Login
            finish();
        });
    }

    private boolean validateForm() {
        boolean ok = true;

        String name = safe(edFullname);
        String email = safe(edEmail);
        String dob = safe(edDob);
        String pw = safe(edPassword);
        String cfpw = safe(edConfirmPassword);

        // Fullname
        if (name.isEmpty()) {
            tilFullname.setError("Please enter your full name");
            ok = false;
        }

        // Email
        if (email.isEmpty()) {
            tilEmail.setError("Please enter email");
            ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email is invalid");
            ok = false;
        }

        // DOB
        if (dob.isEmpty()) {
            tilDob.setError("Please select your date of birth");
            ok = false;
        }

        // Password length + strength (tối thiểu 6)
        if (pw.isEmpty()) {
            tilPassword.setError("Please enter password");
            ok = false;
        } else if (pw.length() < 6) {
            tilPassword.setError("Use at least 6 characters");
            ok = false;
        } else if (!pw.matches("^(?=.*[A-Z])(?=.*\\d).{6,}$")) {
            tilPassword.setError("Min 6 chars, include 1 uppercase & 1 number");
            ok = false;
        }

        // Confirm match
        if (cfpw.isEmpty()) {
            tilConfirmPassword.setError("Please re-enter password");
            ok = false;
        } else if (!pw.equals(cfpw)) {
            tilConfirmPassword.setError("Passwords do not match");
            ok = false;
        }

        return ok;
    }

    private String safe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void hideKeyboard(View v) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }

    // --- small TextWatcher helper ----
    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final Runnable after;
        SimpleTextWatcher(Runnable after) { this.after = after; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(android.text.Editable s) { if (after != null) after.run(); }
    }
}
