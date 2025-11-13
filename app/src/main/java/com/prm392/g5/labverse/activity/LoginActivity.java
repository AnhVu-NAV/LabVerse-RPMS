package com.prm392.g5.labverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.prm392.g5.labverse.BuildConfig;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.activity.forgotPassword.ForgotPasswordActivity;
import com.prm392.g5.labverse.activity.team.ListMyTeamsActivity;
import com.prm392.g5.labverse.activity.team.ListTeamOfPiActivity;
import com.prm392.g5.labverse.activity.team.MyInvitationsActivity;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dto.auth.LoginRequest;
import com.prm392.g5.labverse.dto.auth.LoginResponse;
import com.prm392.g5.labverse.dto.auth.LoginWGoogleRequest;
import com.prm392.g5.labverse.repository.AuthRepository;
import com.prm392.g5.labverse.util.ApiErrorHandler;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {

    private EditText edEmail, edPassword;
    private Button btnLogin, btnLoginWGg;
    private TextView tvSignUpAction, tvLoginError;

    private AuthRepository authRepository = new AuthRepository();

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private final String WEB_CLIENT_ID = BuildConfig.WEB_CLIENT_ID;

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
        tvSignUpAction = findViewById(R.id.tvSignUpAction);
        tvLoginError = findViewById(R.id.tvLoginError);

        //normal login
        btnLogin.setOnClickListener(v -> {
            String email = edEmail.getText().toString().trim();
            String password = edPassword.getText().toString().trim();

            // Kiểm tra email
            if (email.isEmpty()) {
                edEmail.setError("Email is required");
                edEmail.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edEmail.setError("Invalid email format");
                edEmail.requestFocus();
                return;
            }

            // Kiểm tra password
            if (password.isEmpty()) {
                edPassword.setError("Password is required");
                edPassword.requestFocus();
                return;
            }
            LoginRequest loginRequest = new LoginRequest(email, password);
            login(loginRequest);
        });

        //login with Google
        btnLoginWGg.setOnClickListener(v -> {
            loginWithGoogle();
        });

        //đăng ký callback
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    } else {
                        Log.w("GOOGLE_LOGIN", "Sign-in cancelled or failed.");
                    }
                }
        );

        //link qua sign up
        tvSignUpAction.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(i);
        });

        //link quên mật khẩu
        findViewById(R.id.tvForgot).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class))
        );
    }

    public void login(LoginRequest loginRequest) {
        authRepository.login(loginRequest, new Callback<LoginResponse>(){
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginRequestSuceess(response.body());
                } else {
                    ApiErrorHandler.handleApiResponseError(LoginActivity.this, response, "Login");
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // request chưa đến được server hoặc không thể đọc được phản hồi
                ApiErrorHandler.handleNetworkFailure(LoginActivity.this, t, "Login");
            }
        });

    }

    /**
     * send login request to backend server, receive http status 200
     * @param loginResponse
     */
    private void handleLoginRequestSuceess(LoginResponse loginResponse){
        //lưu access token để sử dụng
        Log.d("Login", "Login successfully");
        SharePreferenceManager prefManager = SharePreferenceManager.getInstance();
        //lưu lại access token cũng như user id của người dùng hiện tại
        prefManager.saveUserAuthData(loginResponse.getAccessToken(), loginResponse.getUserId(), loginResponse.getUserRole());
        runOnUiThread(() ->
                Toast.makeText(LoginActivity.this, "Login successfully!", Toast.LENGTH_LONG).show()
        );

        //xem người dùng đăng nhập lần đầu hay là lần 2, để xem vào trang chọn role hay là vào Library của Tuấn Anh luôn
        if (loginResponse.getUserRole() == null || loginResponse.getUserRole().isEmpty()) {
            //chưa chọn role, chuyển qua chọn role
            Intent intent = new Intent(this, RoleSelectActivity.class);
            startActivity(intent);
        } else {
            Log.d("Login", "User has role: " + prefManager.getUserRole());

            //đã chọn role, chuyển qua Library
            Intent intent = new Intent(this, MyLibraryActivity.class);
            startActivity(intent);
        }
        finish();
    }

    public void loginWithGoogle() {
        //set up dể lát hiện cái chọn tk có sẵn trong máy ra đó
        CredentialManager credentialManager = CredentialManager.create(this);
        Executor executor = Executors.newSingleThreadExecutor();

        // Cấu hình đăng nhập Google
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // Gọi đăng nhập
        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                executor,
                new CredentialManagerCallback()
        );
    }

    /**
     * hiện cái chọn tk có sẵn trong máy ra đó
     */
    private class CredentialManagerCallback implements androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
        @Override
        public void onResult(GetCredentialResponse response) {
            try {
                GoogleIdTokenCredential credential =
                        GoogleIdTokenCredential.createFrom(response.getCredential().getData());
                String idToken = credential.getIdToken();
                //này là ok. xác thực tk gg có sẵn trong máy xong rồi nè, ok rồi nè
                Log.d("GOOGLE_ID_TOKEN", idToken);
                Log.d("GOOGLE_AUTHEN", "login local success, calling to backend server to get access token");

                // Gửi idToken lên backend
                loginWGgToBackend(new LoginWGoogleRequest(idToken));
            } catch (Exception e) {
                Log.e("GOOGLE_LOGIN", "Lỗi lấy credential", e);
            }
        }

        @Override
        public void onError(GetCredentialException e) {
            if (e instanceof androidx.credentials.exceptions.NoCredentialException) {

                Log.e("GOOGLE_LOGIN", "Credential exception type: " + e.getClass().getSimpleName()
                        + ", message: " + e.getMessage());

                //oh no, fail rồi, chưa có tk gg nào có sẵn trên máy cả
                Log.d("GOOGLE_LOGIN", "Không tìm thấy tài khoản Google nào. Mở trình chọn tài khoản...");
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this,
                            "No existing Google account found. Please choose an account to sign in.",
                            Toast.LENGTH_SHORT).show();
                });
                startLegacyGoogleSignIn(); // gọi tới cái mở trang đăng nhập tk gg ra nè
            } else {
                Log.d("GOOGLE_LOGIN", "Đăng nhập thất bại: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this,
                                "Login failed",
                                Toast.LENGTH_SHORT).show()
                );
            }
            Log.e("GOOGLE_LOGIN", "GetCredentialException", e);
        }
    }
    private void loginWGgToBackend(LoginWGoogleRequest request) {
        // Gửi idtoken tới backend
        authRepository.loginWGoogle(request, new Callback<LoginResponse>(){
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginRequestSuceess(response.body());
                } else {
                    ApiErrorHandler.handleApiResponseError(LoginActivity.this, response, "LoginWGgToBackend");
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                ApiErrorHandler.handleNetworkFailure(LoginActivity.this, t, "LoginWGgToBackend");
            }
        });
    }

    /**
     * call when the device has no account login on
     * tạo intent để start cái login account ra
     */
    private void startLegacyGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    /**
     * handle the response from startLegacyGoogleSignIn
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String idToken = account.getIdToken();
                Log.d("LEGACY_GOOGLE_TOKEN", idToken);
                loginWGgToBackend(new LoginWGoogleRequest(idToken));
            }
        } catch (ApiException e) {
            Log.w("GOOGLE_LOGIN", "Sign-in failed: " + e.getStatusCode());
        }
    }
}