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
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dto.ErrorResponse;
import com.prm392.g5.labverse.dto.auth.LoginRequest;
import com.prm392.g5.labverse.dto.auth.LoginResponse;
import com.prm392.g5.labverse.dto.auth.LoginWGoogleRequest;
import com.prm392.g5.labverse.repository.AuthRepository;


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {
    //todo sửa giao diện

    private EditText edEmail, edPassword;
    private Button btnLogin, btnLoginWGg;
    private TextView tvSignUp, tvLoginError;

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
        //todo xử lí sign up
        tvSignUp= findViewById(R.id.tvSignUp);
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
            //todo, nhảy qua chọn role xong hẵng đăng nhập, tạm thời server đang để mặc đinhj là Intern hết
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

    }

    public void login(LoginRequest loginRequest) {
        authRepository.login(loginRequest, new Callback<LoginResponse>(){
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginRequestSuceess(response.body());
                } else {
                    handleLoginRequestFail(response);
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // request chưa đến được server hoặc không thể đọc được phản hồi
                handleSendRequestFail(t);
            }
        });

    }

    /**
     * send login request to backend server, receive http status 200
     * @param loginResponse
     */
    private void handleLoginRequestSuceess(LoginResponse loginResponse){
        //lưu access token để sử dụng
        Log.d("Login", "Login success fully");
        SharePreferenceManager prefManager = SharePreferenceManager.getInstance();
        //lưu lại access token cũng như user id của người dùng hiện tại
        prefManager.saveAccessToken(loginResponse.getAccessToken());
        prefManager.saveUserId(loginResponse.getUserId());
        runOnUiThread(() ->
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_LONG).show()
        );
        //todo chuyển người dùng qua activity khác
    }

    /**
     * send login request to backend server, but receive response not 200 OK
     * @param response
     */
    private void handleLoginRequestFail(Response<LoginResponse> response) {
        try(ResponseBody errorBody = response.errorBody()) {
            // Nếu không có error body thì dừng sớm, tránh lồng if
            if (errorBody == null) {
                Log.e("Login", "Empty error body");
                tvLoginError.setText("Unknown error");
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
            tvLoginError.setText(message);

        } catch (IOException e) {
            Log.e("Login", "Failed to parse error response", e);
            tvLoginError.setText("Something went wrong");
        }
    }

    /**
     * called when can not send the request
     * không thể gửi request về server backend
     *
     * @param t
     */
    private void handleSendRequestFail(Throwable t){
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
                //oh no, fail rồi, chưa có tk gg nào có sẵn trên máy cả
                Log.d("GOOGLE_LOGIN", "Không tìm thấy tài khoản Google nào. Mở trình chọn tài khoản...");
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this,
                            "Không tìm thấy tài khoản Google nào. Mở trình chọn tài khoản...",
                            Toast.LENGTH_SHORT).show();
                });
                startLegacyGoogleSignIn(); // gọi tới cái mở trang đăng nhập tk gg ra nè
            } else {
                Log.d("GOOGLE_LOGIN", "Đăng nhập thất bại");
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this,
                                "Đăng nhập thất bại: " + e.getMessage(),
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
                    handleLoginRequestFail(response);
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                handleSendRequestFail(t);
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