package ute.nhom27.android.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.R;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.api.LoginRequest;
import ute.nhom27.android.model.User;
import ute.nhom27.android.utils.SharedPrefManager;
import ute.nhom27.android.view.MainActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etPhoneOrEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private SharedPrefManager sharedPrefManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo các view từ layout
        etPhoneOrEmail = findViewById(R.id.editTextPhoneOrEmail);
        etPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        tvForgotPassword = findViewById(R.id.textViewForgotPassword);

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
            // TODO: Thêm logic cho chức năng quên mật khẩu sau này
        });
    }

    private void loginUser() {
        String phoneOrEmail = etPhoneOrEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Kiểm tra đầu vào
        if (phoneOrEmail.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại/email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị trạng thái đang xử lý
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        // Tạo request body
        LoginRequest loginRequest = new LoginRequest(phoneOrEmail, password);

        // Gọi API đăng nhập
        Call<User> call = apiService.login(loginRequest);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");

                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    // Lưu thông tin người dùng
                    sharedPrefManager.saveUser(user);

                    // Chuyển sang MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: Thông tin không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
                Toast.makeText(LoginActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
