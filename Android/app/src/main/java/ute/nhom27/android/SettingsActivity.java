package ute.nhom27.android;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;

import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.api.ThemeUpdateRequest;
import ute.nhom27.android.model.User;
import ute.nhom27.android.utils.SharedPrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends BaseActivity {

    private Spinner spinnerTheme;
    private Button btnSaveTheme;
    private SharedPrefManager sharedPrefManager;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        spinnerTheme = findViewById(R.id.spinnerTheme);
        btnSaveTheme = findViewById(R.id.btnSaveTheme);
        sharedPrefManager = new SharedPrefManager(this);
        apiService = ApiClient.getAuthClient(this).create(ApiService.class);

        // Thiết lập Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.theme_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(adapter);

        // Chọn theme hiện tại
        String currentTheme = sharedPrefManager.getThemePreference();
        if ("dark".equals(currentTheme)) {
            spinnerTheme.setSelection(1);
        } else {
            spinnerTheme.setSelection(0);
        }

        btnSaveTheme.setOnClickListener(v -> saveTheme());
    }

    private void saveTheme() {
        String themePreference = spinnerTheme.getSelectedItem().toString();
        sharedPrefManager.saveThemePreference(themePreference);
        applyTheme();

        // Gửi yêu cầu cập nhật theme lên backend
        ThemeUpdateRequest request = new ThemeUpdateRequest();
        request.setThemePreference(themePreference);
        String token = "Bearer " + sharedPrefManager.getToken();

        // Thêm logging để debug
        Log.d("Token", "Token: " + token);
        Log.d("TokenThem", "Theme preference: " + themePreference);

        Call<User> call = apiService.updateTheme(request, token);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sharedPrefManager.saveUser(response.body());
                    Toast.makeText(SettingsActivity.this, "Đã lưu giao diện: " + themePreference, Toast.LENGTH_SHORT).show();
                } else {
                    // Thêm logging để debug lỗi
                    Log.e("SettingsActivity", "Error response code: " + response.code());
                    try {
                        Log.e("SettingsActivity", "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(SettingsActivity.this, "Lỗi khi cập nhật theme", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(SettingsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
