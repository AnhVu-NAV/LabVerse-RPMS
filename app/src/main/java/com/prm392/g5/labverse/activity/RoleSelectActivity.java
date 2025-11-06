package com.prm392.g5.labverse.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.prm392.g5.labverse.R;

public class RoleSelectActivity extends AppCompatActivity {

    private MaterialCardView cardPi, cardResearcher, cardStudent;
    private MaterialButton btnContinue;

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
        };

        cardPi.setOnClickListener(pick);
        cardResearcher.setOnClickListener(pick);
        cardStudent.setOnClickListener(pick);

        // (tuỳ chọn) default chọn card 1:
//         setPicked(cardPi);
//         btnContinue.setEnabled(true);
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
