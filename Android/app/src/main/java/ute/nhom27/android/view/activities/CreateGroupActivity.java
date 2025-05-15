package ute.nhom27.android.view.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.R;
import ute.nhom27.android.adapter.FriendSelectAdapter;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.model.ChatGroup;
import ute.nhom27.android.model.User;
import ute.nhom27.android.model.response.UserResponse;
import ute.nhom27.android.utils.SharedPrefManager;

public class CreateGroupActivity extends AppCompatActivity {
    private TextInputEditText etGroupName;
    private RecyclerView rvFriends;
    private MaterialButton btnCreateGroup;
    private FriendSelectAdapter adapter;
    private ApiService apiService;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        initViews();
        setupRecyclerView();
        loadFriends();
        setupCreateButton();
    }

    private void initViews() {
        etGroupName = findViewById(R.id.et_group_name);
        rvFriends = findViewById(R.id.rv_friends);
        btnCreateGroup = findViewById(R.id.btn_create_group);
        sharedPrefManager = new SharedPrefManager(this);
        apiService = ApiClient.getAuthClient(this).create(ApiService.class);
    }

    private void setupRecyclerView() {
        adapter = new FriendSelectAdapter();
        rvFriends.setLayoutManager(new LinearLayoutManager(this));
        rvFriends.setAdapter(adapter);
    }

    private void loadFriends() {
        Long userId = sharedPrefManager.getUser().getId();
        apiService.getFriends(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserResponse> friends = response.body();
                    adapter.setFriends(friends);
                } else {
                    Toast.makeText(CreateGroupActivity.this, "Lỗi dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                Toast.makeText(CreateGroupActivity.this, "Không thể tải danh sách bạn bè", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCreateButton() {
        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            if (TextUtils.isEmpty(groupName)) {
                etGroupName.setError("Vui lòng nhập tên nhóm");
                return;
            }

            if (adapter.getSelectedFriends().isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một thành viên", Toast.LENGTH_SHORT).show();
                return;
            }

            createGroup(groupName);
        });
    }

    private void createGroup(String groupName) {
        Long userId = sharedPrefManager.getUser().getId();
        apiService.createGroup(groupName, userId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseData = response.body();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> groupData = (Map<String, Object>) responseData.get("group");
                    if (groupData != null && groupData.get("id") != null) {
                        Long groupId = ((Number) groupData.get("id")).longValue();
                        addMembersToGroup(groupId);
                    } else {
                        Toast.makeText(CreateGroupActivity.this, "Lỗi tạo nhóm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateGroupActivity.this, "Không thể tạo nhóm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(CreateGroupActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMembersToGroup(Long groupId) {
        int totalMembers = adapter.getSelectedFriends().size();
        int[] addedMembers = {0};

        for (Long memberId : adapter.getSelectedFriends()) {
            apiService.addGroupMember(groupId, memberId).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    addedMembers[0]++;
                    if (addedMembers[0] == totalMembers) {
                        Toast.makeText(CreateGroupActivity.this, "Tạo nhóm thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    addedMembers[0]++;
                    if (addedMembers[0] == totalMembers) {
                        Toast.makeText(CreateGroupActivity.this, "Đã thêm một số thành viên", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }
    }
} 