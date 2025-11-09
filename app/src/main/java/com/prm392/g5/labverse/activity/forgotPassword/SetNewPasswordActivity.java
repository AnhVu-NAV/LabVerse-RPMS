package com.prm392.g5.labverse.activity.forgotPassword;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.repository.AuthRepository;
import com.prm392.g5.labverse.util.ApiErrorHandler;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetNewPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilPassword, tilConfirm;
    private TextInputEditText edPassword, edConfirm;

    private String email;
    private String resetPassToken;

    public static void open(String email, String resetPassToken, Context context) {
        Intent intent = new Intent(context, SetNewPasswordActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("resetPassToken", resetPassToken);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_password);

        email = getIntent().getStringExtra("email");
        resetPassToken = getIntent().getStringExtra("resetPassToken");

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

            //call backend API to set new password with reset token
            AuthRepository authRepository = new AuthRepository();
            authRepository.resetPassword(email, resetPassToken, p1, new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Intent intent = new Intent(SetNewPasswordActivity.this, PasswordSuccessActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        ApiErrorHandler.handleApiResponseError(SetNewPasswordActivity.this, response, "ResetPassword");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    ApiErrorHandler.handleNetworkFailure(SetNewPasswordActivity.this, t, "ResetPassword");
                }
            });
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
