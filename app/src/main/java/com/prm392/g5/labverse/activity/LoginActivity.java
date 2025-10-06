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
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dto.auth.LoginRequest;
import com.prm392.g5.labverse.dto.auth.LoginResponse;
import com.prm392.g5.labverse.repository.AuthRepository;

import org.json.JSONObject;

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
        btnLogin.setOnClickListener(v -> {
            //todo check email valid
            //cần đảm bảo cả email, password đều tồn tại trước khi guiwr ddi

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
                    //lưu access token để sử dụng
                    Log.d("Login", "Login success fully");
                    SharePreferenceManager prefManager = SharePreferenceManager.getInstance();

                    prefManager.saveAccessToken(response.body().getAccessToken());
                    //todo chuyển người dùng qua activity khác
                } else {
                    // todo: handle error: response.code(), response.errorBody()
                    //chưa biết hiện ra ntn
                    try {
                        if (response.errorBody() != null) {
                            // Đọc nội dung JSON từ errorBody
                            String errorJson = response.errorBody().string();
                            Log.d("error response", errorJson);
                            JSONObject json = new JSONObject(errorJson);

                            // Lấy message từ JSON
                            String message = json.optString("message", "Unknown error");
                            int code = json.optInt("code", response.code()); // fallback nếu không có

                            tvLoginError.setText(message);
                            Log.e("Login", "Error " + code + ": " + message);
                        } else {
                            tvLoginError.setText("Unknown error");
                            Log.e("Login", "Empty error body");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tvLoginError.setText("Something went wrong");
                    }

                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // request chưa đến được server hoặc không thể đọc được phản hồi
                //todo handle network failure
                //todo xin quyeenf truy caapj internet
                Log.e("Login", "Request failed", t);

                if (t instanceof java.net.UnknownHostException) {
                    tvLoginError.setText("Không có kết nối mạng. Vui lòng kiểm tra Internet.");
                } else if (t instanceof java.net.SocketTimeoutException) {
                    tvLoginError.setText("Kết nối bị hết hạn. Vui lòng thử lại.");
                } else if (t instanceof java.net.ConnectException) {
                    tvLoginError.setText("Không thể kết nối tới máy chủ.");
                } else if (t instanceof javax.net.ssl.SSLException) {
                    tvLoginError.setText("Lỗi chứng chỉ bảo mật.");
                } else {
                    tvLoginError.setText("Đã xảy ra lỗi không xác định. Vui lòng thử lại.");
                }

            }
        });

    }
}