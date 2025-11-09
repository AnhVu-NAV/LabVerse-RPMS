package com.prm392.g5.labverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dto.user.UserSimpleResponse;
import com.prm392.g5.labverse.repository.UserRepository;
import com.prm392.g5.labverse.util.ApiErrorHandler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoleSelectActivity extends AppCompatActivity {

    private MaterialCardView cardPi, cardResearcher, cardStudent;
    private MaterialButton btnContinue;
    private String selectedRole = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_select);

        cardPi = findViewById(R.id.cardPi);
        cardResearcher = findViewById(R.id.cardResearcher);
        cardStudent = findViewById(R.id.cardStudent);
        btnContinue = findViewById(R.id.btnContinue);

        View.OnClickListener pick = v -> {
            setPicked((MaterialCardView) v);
            btnContinue.setEnabled(true);

            if (v == cardPi) {
                selectedRole = "PI";
            } else if (v == cardResearcher) {
                selectedRole = "RESEARCHER";
            } else if (v == cardStudent) {
                selectedRole = "INTERN";
            }
        };

        cardPi.setOnClickListener(pick);
        cardResearcher.setOnClickListener(pick);
        cardStudent.setOnClickListener(pick);

        // (tuỳ chọn) default chọn card 1:
//         setPicked(cardPi);
//         btnContinue.setEnabled(true);

        SharePreferenceManager sharePreferenceManager = SharePreferenceManager.getInstance();
        String email = sharePreferenceManager.getUserId();

        btnContinue.setOnClickListener(v -> {
            if (selectedRole == null) {
                Toast.makeText(this, "Please select a role first", Toast.LENGTH_SHORT).show();
                return;
            }

            UserRepository userRepository = new UserRepository();

            //gọi api backend để set role
            userRepository.selectRole(email, selectedRole, new Callback<UserSimpleResponse>() {
                @Override
                public void onResponse(Call<UserSimpleResponse> call, Response<UserSimpleResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        //set role vào trong SharedPreferences
                        sharePreferenceManager.saveUserRole(response.body().getRoleName());

                        Toast.makeText(RoleSelectActivity.this, "Role set successfully", Toast.LENGTH_LONG).show();
                        //todo  Chuyển sang màn hình LIBRARY của Tuấn Anh
//                        startActivity(new Intent(RoleSelectActivity.this, MainActivity.class));
//                        finish();
                    } else {
                        ApiErrorHandler.handleApiResponseError(RoleSelectActivity.this, response, "SelectRole");
                    }
                }

                @Override
                public void onFailure(Call<UserSimpleResponse> call, Throwable t) {
                    ApiErrorHandler.handleNetworkFailure(RoleSelectActivity.this, t, "SelectRole");
                }
            });
        });
    }

    private void setPicked(MaterialCardView picked) {
        MaterialCardView[] all = {cardPi, cardResearcher, cardStudent};
        for (MaterialCardView c : all) {
            boolean selected = (c == picked);
            c.setSelected(selected);

            // hiệu ứng "nổi lên" nhẹ khi được chọn
            c.setStrokeWidth(selected ? dp(2) : dp(1));
            c.setCardElevation(selected ? dp(4) : dp(0));
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
