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
import com.prm392.g5.labverse.dto.auth.LoginResponse;
import com.prm392.g5.labverse.dto.user.RegisterAccountRequest;
import com.prm392.g5.labverse.dto.user.UserSimpleResponse;
import com.prm392.g5.labverse.repository.UserRepository;

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
                        //todo chuyển qua trang nhập OTP verify account
                        Toast.makeText(SignUpActivity.this, "Sign up successfully, please check your email to get OTP", Toast.LENGTH_SHORT).show();
                    } else {
                        handleSignUpRequestFail(response);
                    }
                }

                @Override
                public void onFailure(Call<UserSimpleResponse> call, Throwable t) {
                    // request chưa đến được server hoặc không thể đọc được phản hồi
                    handleSendRequestFail(t);
                }
            });
        });
    }

    private void handleSignUpRequestFail(Response<UserSimpleResponse> response) {
        try(ResponseBody errorBody = response.errorBody()) {
            // Nếu không có error body thì dừng sớm, tránh lồng if
            if (errorBody == null) {
                Log.e("Login", "Empty error body");
                Toast.makeText(SignUpActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                return;
            }

            // Dùng Retrofit converter để parse errorBody thành ErrorResponse
            Converter<ResponseBody, ErrorResponse> converter =
                    RetrofitClient.getInstance()
                            .responseBodyConverter(ErrorResponse.class, new Annotation[0]);
            ErrorResponse errorResponse = converter.convert(response.errorBody());

            //parse thành công
            if (errorResponse == null) {
                throw new IOException("ErrorResponse is null");
            }

            int code = errorResponse.getCode();
            String message = errorResponse.getMessage();

            //TODO THIẾT LẬP CƠ CHẾ XỬ LÍ LỖIIIIIIII

            Log.e("Login", "Error " + code + ": " + message);
            Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();


        } catch (IOException e) {
            Log.e("Login", "Failed to parse error response", e);
            Toast.makeText(SignUpActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSendRequestFail(Throwable t){
        Log.e("Login", "Request failed", t);

        if (t instanceof java.net.UnknownHostException) {
            Toast.makeText(SignUpActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
        } else if (t instanceof java.net.SocketTimeoutException) {
            Toast.makeText(SignUpActivity.this, "Timeout!", Toast.LENGTH_SHORT).show();
        } else if (t instanceof java.net.ConnectException) {
            Toast.makeText(SignUpActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
        } else if (t instanceof javax.net.ssl.SSLException) {
            Toast.makeText(SignUpActivity.this, "SSL Exception", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(SignUpActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
        }
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
