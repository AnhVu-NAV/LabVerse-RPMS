package com.prm392.g5.labverse.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.repository.UserRepository;
import com.prm392.g5.labverse.util.ApiErrorHandler;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyAccountActivity extends AppCompatActivity {

    private final EditText[] ets = new EditText[6];
    private MaterialButton btnVerify;

    private String email;

    public static void open(String email, Context context) {
        Intent intent = new Intent(context, VerifyAccountActivity.class);
        intent.putExtra("email", email);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        email = getIntent().getStringExtra("email");

        // Back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // OTP boxes
        ets[0] = findViewById(R.id.et1);
        ets[1] = findViewById(R.id.et2);
        ets[2] = findViewById(R.id.et3);
        ets[3] = findViewById(R.id.et4);
        ets[4] = findViewById(R.id.et5);
        ets[5] = findViewById(R.id.et6);

        btnVerify = findViewById(R.id.btnVerify);
        btnVerify.setEnabled(false);

        UserRepository userRepository = new UserRepository();
        // Resend OTP (TextView đã tách làm 2 phần trong XML)
        TextView tvResendOtpAction = findViewById(R.id.tvResendOtpAction);
        tvResendOtpAction.setOnClickListener(v -> {
            // call resend API here
            userRepository.resendOtpVerifyAccount(email, new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(VerifyAccountActivity.this, "Resent successfully, please check your email to get OTP", Toast.LENGTH_LONG).show();

                    } else {
                        ApiErrorHandler.handleApiResponseError(VerifyAccountActivity.this, response, "ResentVerifyAccount");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // request chưa đến được server hoặc không thể đọc được phản hồi
                    ApiErrorHandler.handleNetworkFailure(VerifyAccountActivity.this, t, "ResentVerifyAccount");
                }

            });
        });

        setupOtpInputs();

        btnVerify.setOnClickListener(v -> {
            String inputOtp = collectCode();
            if (inputOtp.length() != 6) {
                Toast.makeText(this, "Please enter 6 digits", Toast.LENGTH_SHORT).show();
                return;
            }
            // call verify API
            userRepository.verifyAccount(email, inputOtp, new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(VerifyAccountActivity.this, "Account verified successfully, now you can login using this account", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(VerifyAccountActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        ApiErrorHandler.handleApiResponseError(VerifyAccountActivity.this, response, "VerifyAccount");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // request chưa đến được server hoặc không thể đọc được phản hồi
                    ApiErrorHandler.handleNetworkFailure(VerifyAccountActivity.this, t, "VerifyAccount");
                }
            });
        });
    }

    private void setupOtpInputs() {
        // mỗi ô chỉ 1 ký tự
        for (EditText et : ets) {
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        }

        for (int i = 0; i < ets.length; i++) {
            final int idx = i;

            ets[idx].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    ets[idx].setActivated(s.length() == 1); // đổi background khi filled

                    // tự chuyển sang ô tiếp theo
                    if (s.length() == 1 && idx < ets.length - 1) {
                        ets[idx + 1].requestFocus();
                        ets[idx + 1].selectAll();
                    }

                    // Enable nút khi đủ 6 số
                    btnVerify.setEnabled(collectCode().length() == 6);
                }
            });

            // Backspace trống -> lùi ô
            ets[idx].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_DEL
                        && ets[idx].getText().length() == 0
                        && idx > 0) {
                    ets[idx - 1].requestFocus();
                    ets[idx - 1].setText("");
                    return true;
                }
                return false;
            });
        }

        // Enter/Done ở ô cuối sẽ bấm Verify
        ets[5].setOnEditorActionListener((v, actionId, event) -> {
            if (btnVerify.isEnabled()) btnVerify.performClick();
            return true;
        });

        ets[0].requestFocus();
    }

    private String collectCode() {
        StringBuilder sb = new StringBuilder();
        for (EditText et : ets) sb.append(et.getText().toString());
        return sb.toString();
    }
}
