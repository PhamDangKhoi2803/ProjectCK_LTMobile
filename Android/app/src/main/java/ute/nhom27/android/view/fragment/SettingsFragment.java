package ute.nhom27.android.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.BaseActivity;
import ute.nhom27.android.R;
import ute.nhom27.android.SettingsActivity;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.api.PasswordChangeRequest;
import ute.nhom27.android.api.ThemeUpdateRequest;
import ute.nhom27.android.model.User;
import ute.nhom27.android.model.response.UserResponse;
import ute.nhom27.android.utils.CloudinaryUtils;
import ute.nhom27.android.utils.SharedPrefManager;
import ute.nhom27.android.view.activities.LoginActivity;

public class SettingsFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Spinner spinnerTheme;
    private Button btnSaveTheme;
    private Button btnLogout;

    private CircleImageView ivAvatar;
    private Button btnChangeAvatar;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvFriendsCount;
    private Button btnChangePassword;

    private SharedPrefManager sharedPrefManager;
    private ApiService apiService;
    private ThemeChange themeChange;

    private ProgressBar progressBar;


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

        // Initialize all views
        initializeViews(view);

        // Initialize SharedPrefManager and ApiService
        if (getContext() != null) {
            sharedPrefManager = new SharedPrefManager(getContext());
            apiService = ApiClient.getAuthClient(getContext()).create(ApiService.class);
        }

        // Setup spinner
        setupSpinner();

        // Load user info
        loadUserInfo();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews(View view) {
        // Existing views
        spinnerTheme = view.findViewById(R.id.spinnerTheme);
        btnSaveTheme = view.findViewById(R.id.btnSaveTheme);
        btnLogout = view.findViewById(R.id.btnLogout);

        // New views
        ivAvatar = view.findViewById(R.id.ivAvatar);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvFriendsCount = view.findViewById(R.id.tvFriendsCount);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
    }

    private void setupSpinner() {
        if (getContext() != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.theme_options, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTheme.setAdapter(adapter);

            String currentTheme = sharedPrefManager.getThemePreference();
            if ("dark".equals(currentTheme)) {
                spinnerTheme.setSelection(1);
            } else {
                spinnerTheme.setSelection(0);
            }
        }
    }

    private void loadUserInfo() {
        User currentUser = sharedPrefManager.getUser();
        if (currentUser != null) {
            tvEmail.setText("Email: " + currentUser.getEmail());
            tvPhone.setText("Số điện thoại: " + currentUser.getPhone());

            // Load avatar từ Cloudinary
            if (currentUser.getAvatarURL() != null && !currentUser.getAvatarURL().isEmpty()) {
                Log.d("User", "Loading avatar from URL: " + currentUser.getAvatarURL());

                Glide.with(requireContext())
                        .load(currentUser.getAvatarURL())
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                Log.d("User", "No avatar URL found, using default avatar");
                ivAvatar.setImageResource(R.drawable.default_avatar);
            }
            // Load friends count
            loadFriendsCount();
        }
    }

    private void loadFriendsCount() {
        User currentUser = sharedPrefManager.getUser();
        if (currentUser == null) return;

        String token = "Bearer " + sharedPrefManager.getToken();
        apiService.getFriendsCount(currentUser.getId(), token).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvFriendsCount.setText("Số bạn bè: " + response.body());
                } else {
                    Toast.makeText(getContext(), "Không thể tải số lượng bạn bè", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        // Existing listeners
        btnSaveTheme.setOnClickListener(v -> saveTheme());
        btnLogout.setOnClickListener(v -> logout());

        // New listeners
        btnChangeAvatar.setOnClickListener(v -> openImagePicker());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadAvatar(imageUri);
        }
    }

    private void uploadAvatar(Uri imageUri) {
        User currentUser = sharedPrefManager.getUser();
        if (currentUser == null) return;

        CloudinaryUtils.uploadImage(requireContext(), imageUri, new CloudinaryUtils.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d("Cloudinary", "Uploaded image URL: " + imageUrl);

                // Lấy token từ SharedPrefManager
                String token = sharedPrefManager.getToken();
                if (token == null || token.isEmpty()) {
                    Toast.makeText(getContext(), "Token không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Log token trước khi thêm prefix
                Log.d("Token Debug", "Original token: " + token);

                // Thêm prefix "Bearer " nếu chưa có
                if (!token.startsWith("Bearer ")) {
                    token = "Bearer " + token;
                }

                // Log token sau khi thêm prefix
                Log.d("Token Debug", "Token with prefix: " + token);

                apiService.updateAvatar(currentUser.getId(), imageUrl, token).enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UserResponse updatedUser = response.body();
                            // Cập nhật user trong SharedPreferences
                            User user = new User();
                            user.setId(updatedUser.getId());
                            user.setUsername(updatedUser.getUsername());
                            user.setEmail(updatedUser.getEmail());
                            user.setPhone(updatedUser.getPhone());
                            user.setAvatarURL(updatedUser.getAvatarURL());
                            sharedPrefManager.saveUser(user);

                            // Load ảnh mới ngay lập tức
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Glide.with(requireContext())
                                            .load(updatedUser.getAvatarURL())
                                            .placeholder(R.drawable.default_avatar)
                                            .error(R.drawable.default_avatar)
                                            .circleCrop()
                                            .into(ivAvatar);
                                });
                            }

                            Toast.makeText(getContext(), "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                Log.e("API", "Error response: " + response.code() + ", Body: " + errorBody);
                            } catch (IOException e) {
                                Log.e("API", "Error reading error body", e);
                            }
                            Toast.makeText(getContext(), "Lỗi khi cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
                        Log.e("API", "Error updating avatar: " + t.getMessage());
                        Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e("Cloudinary", "Upload error: " + error);
                Toast.makeText(getContext(), "Lỗi khi upload ảnh: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(requireContext(), uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int columnIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(columnIdx);
        cursor.close();
        return result;
    }

    private void showChangePasswordDialog() {
        if (getContext() == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Đổi mật khẩu")
                .setView(dialogView)
                .setPositiveButton("Đổi mật khẩu", null)
                .setNegativeButton("Hủy", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String currentPassword = etCurrentPassword.getText().toString();
                String newPassword = etNewPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(getContext(), "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                changePassword(currentPassword, newPassword);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void changePassword(String currentPassword, String newPassword) {
        User currentUser = sharedPrefManager.getUser();
        if (currentUser == null) return;

        String token = "Bearer " + sharedPrefManager.getToken();
        PasswordChangeRequest request = new PasswordChangeRequest(currentPassword, newPassword);

        apiService.changePassword(currentUser.getId(), request, token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Có", (dialog, which) -> {
                    sharedPrefManager.clear();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                    Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Không", null)
                .show();
    }
}
