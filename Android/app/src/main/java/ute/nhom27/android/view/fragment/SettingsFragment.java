package ute.nhom27.android.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.BaseActivity;
import ute.nhom27.android.R;
import ute.nhom27.android.SettingsActivity;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.api.ThemeUpdateRequest;
import ute.nhom27.android.model.User;
import ute.nhom27.android.utils.SharedPrefManager;
import ute.nhom27.android.view.activities.LoginActivity;

public class SettingsFragment extends Fragment {

    private Spinner spinnerTheme;
    private Button btnSaveTheme;
    private SharedPrefManager sharedPrefManager;
    private ApiService apiService;
    private ThemeChange themeChange;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof ThemeChange) {
            themeChange = (ThemeChange) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ThemeChangeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        themeChange = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout cho Fragment
        return inflater.inflate(R.layout.activity_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các view
        spinnerTheme = view.findViewById(R.id.spinnerTheme);
        btnSaveTheme = view.findViewById(R.id.btnSaveTheme);

        // Khởi tạo SharedPrefManager và ApiService
        if (getContext() != null) {
            sharedPrefManager = new SharedPrefManager(getContext());
            apiService = ApiClient.getAuthClient(getContext()).create(ApiService.class);

            // Thiết lập Spinner
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
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
        }

        // Xử lý sự kiện nút Save Theme
        btnSaveTheme.setOnClickListener(v -> saveTheme());
    }

    private void saveTheme() {
        if (getContext() == null) return;
        
        String themePreference = spinnerTheme.getSelectedItem().toString();
        sharedPrefManager.saveThemePreference(themePreference);
        if (themeChange != null) {
            themeChange.onThemeChange();
        }
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
                if (getContext() == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    sharedPrefManager.saveUser(response.body());
                    Toast.makeText(getContext(), "Đã lưu giao diện: " + themePreference, Toast.LENGTH_SHORT).show();
                } else {
                    // Thêm logging để debug lỗi
                    Log.e("SettingsActivity", "Error response code: " + response.code());
                    try {
                        Log.e("SettingsActivity", "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Lỗi khi cập nhật theme", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
