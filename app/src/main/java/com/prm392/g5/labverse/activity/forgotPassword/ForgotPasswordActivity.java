package com.prm392.g5.labverse.activity.forgotPassword;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.repository.AuthRepository;
import com.prm392.g5.labverse.util.ApiErrorHandler;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

            //gọi API reset password (send email)
            AuthRepository authRepository = new AuthRepository();
            authRepository.forgotPassword(email, new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "OTP has been sent to the email", Toast.LENGTH_LONG).show();
                        VerifyForgotPasswordActivity.open(email, ForgotPasswordActivity.this);
                        finish();
                    } else {
                        ApiErrorHandler.handleApiResponseError(ForgotPasswordActivity.this, response, "ForgotPassword");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // request chưa đến được server hoặc không thể đọc được phản hồi
                    ApiErrorHandler.handleNetworkFailure(ForgotPasswordActivity.this, t, "ForgotPassword");
                    finish(); // quay lại Login
                }
            });
        });
    }
}
