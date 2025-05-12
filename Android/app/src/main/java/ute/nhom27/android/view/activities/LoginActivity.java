package ute.nhom27.android.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.R;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.api.LoginRequest;
import ute.nhom27.android.api.LoginResponse;
import ute.nhom27.android.model.User;
import ute.nhom27.android.utils.SharedPrefManager;
import ute.nhom27.android.view.MainActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etPhoneOrEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private SharedPrefManager sharedPrefManager;
    private ApiService apiService;
    private TextView textViewRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo các view
        etPhoneOrEmail = findViewById(R.id.editTextPhoneOrEmail);
        etPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        tvForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewRegister = findViewById(R.id.textViewRegister);

        // Khởi tạo SharedPreferences và Retrofit
        sharedPrefManager = new SharedPrefManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (sharedPrefManager.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Xử lý sự kiện nút Login
        btnLogin.setOnClickListener(v -> loginUser());

        // Xử lý sự kiện "Quên mật khẩu"
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Forgot Password feature is not implemented yet", Toast.LENGTH_SHORT).show();
        });
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String phoneOrEmail = etPhoneOrEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (phoneOrEmail.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại/email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        LoginRequest loginRequest = new LoginRequest(phoneOrEmail, password);
        Log.d("LoginRequest", "Sending: " + new Gson().toJson(loginRequest));

        Call<LoginResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    User user = loginResponse.getUser();
                    String token = loginResponse.getToken();

                    // Lưu thông tin người dùng và token
                    sharedPrefManager.saveUser(user);
                    sharedPrefManager.saveToken(token);

                    // Chuyển sang MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    //Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: Thông tin không hợp lệ", Toast.LENGTH_SHORT).show();
                    String errorMessage = "Đăng nhập thất bại: Thông tin không hợp lệ";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    errorMessage += " (Code: " + response.code() + ")";
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("LoginError", "Response Code: " + response.code() + ", Message: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
                Toast.makeText(LoginActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                Log.e("LoginError", "Failure: " + t.getMessage());
            }
        });
    }
}