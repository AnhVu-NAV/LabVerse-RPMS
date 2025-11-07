package com.prm392.g5.labverse.activity;

import android.os.Bundle;
import android.util.Log;
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
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dto.ErrorResponse;
import com.prm392.g5.labverse.dto.user.RegisterAccountRequest;
import com.prm392.g5.labverse.dto.user.UserSimpleResponse;
import com.prm392.g5.labverse.repository.UserRepository;
import com.prm392.g5.labverse.util.ApiErrorHandler;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    // TextInputLayout
    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword;
    // EditText
    private TextInputEditText edEmail, edPassword, edConfirmPassword;
    // Buttons / actions
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        bindViews();
        setupLiveErrorClearing();
        setupActions();
    }

    private void bindViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        edConfirmPassword = findViewById(R.id.edConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);

        // "Login" quay lại màn trước
        findViewById(R.id.tvLogin).setOnClickListener(v -> finish());
    }

    private void setupLiveErrorClearing() {
        // mỗi khi gõ lại thì clear error
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

            String email = safe(edEmail);
            String password = safe(edPassword);
            // gọi API đăng ký ở đây
            UserRepository userRepository = new UserRepository();
            RegisterAccountRequest request = new RegisterAccountRequest(password, email);
            userRepository.registerAccount(request, new Callback<UserSimpleResponse>() {
                @Override
                public void onResponse(Call<UserSimpleResponse> call, Response<UserSimpleResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(SignUpActivity.this, "Sign up successfully, please check your email to get OTP", Toast.LENGTH_SHORT).show();

                        //chuyển qua trang nhập OTP verify account
                        VerifyAccountActivity.open(email, SignUpActivity.this);
                    } else {
                        ApiErrorHandler.handleApiResponseError(SignUpActivity.this, response, "SignUp");
                    }
                }

                @Override
                public void onFailure(Call<UserSimpleResponse> call, Throwable t) {
                    // request chưa đến được server hoặc không thể đọc được phản hồi
                    ApiErrorHandler.handleNetworkFailure(SignUpActivity.this, t, "SignUp");
                }
            });
        });
    }

    private boolean validateForm() {
        boolean ok = true;

        String email = safe(edEmail);
        String pw = safe(edPassword);
        String cfpw = safe(edConfirmPassword);

        // Email
        if (email.isEmpty()) {
            tilEmail.setError("Please enter email");
            ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email is invalid");
            ok = false;
        }

        // Password length + strength (tối thiểu 6)
        if (pw.isEmpty()) {
            tilPassword.setError("Please enter password");
            ok = false;
        } else if (pw.length() < 6) {
            tilPassword.setError("Use at least 6 characters");
            ok = false;
        } else if (!pw.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            tilPassword.setError("Min 8 chars, include at least 1 letter and 1 number");
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
