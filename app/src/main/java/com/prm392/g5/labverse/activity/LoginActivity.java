package com.prm392.g5.labverse.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.dto.auth.LoginRequest;
import com.prm392.g5.labverse.dto.auth.LoginResponse;
import com.prm392.g5.labverse.repository.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    //todo sửa giao diện

    private EditText edEmail, edPassword;
    private Button btnLogin, btnLoginWGg;
    private TextView tvSignUp, tvLoginError;

    private AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginWGg = findViewById(R.id.btnLoginWGg);
        tvSignUp= findViewById(R.id.tvSignUp);
        tvLoginError = findViewById(R.id.tvLoginError);

        //normal login
        //todo check email valid
        btnLogin.setOnClickListener(v -> {
            LoginRequest loginRequest = new LoginRequest(edEmail.getText().toString(), edPassword.getText().toString());
            login(loginRequest);
        });

        //login with Google
        btnLoginWGg.setOnClickListener(v -> {

        });

    }

    public void login(LoginRequest loginRequest) {
        authRepository.login(loginRequest, new Callback<LoginResponse>(){
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //todo lưu access token để sử dụng
                    Log.d("Login", "Login success fully");
                } else {
                    // handle error: response.code(), response.errorBody()
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                //todo handle network failure
                //todo xin quyeenf truy caapj internet

            }
        });

    }
}