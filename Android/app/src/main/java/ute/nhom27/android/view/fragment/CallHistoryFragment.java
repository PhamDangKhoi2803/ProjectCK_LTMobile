package ute.nhom27.android.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.R;
import ute.nhom27.android.adapter.CallHistoryAdapter;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.model.response.CallHistoryResponse;
import ute.nhom27.android.model.response.UserResponse;
import ute.nhom27.android.utils.SharedPrefManager;
import ute.nhom27.android.view.activities.ChatActivity;

public class CallHistoryFragment extends Fragment implements CallHistoryAdapter.OnCallHistoryItemClickListener {
    private RecyclerView rvCallHistory;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private ApiService apiService;
    private SharedPrefManager prefManager;
    private CallHistoryAdapter adapter;
    private List<CallHistoryResponse> callHistoryList;
    private Map<Long, UserResponse> userInfoMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_call_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo views
        rvCallHistory = view.findViewById(R.id.rvCallHistory);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        // Khởi tạo Toolbar
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Khởi tạo danh sách và adapter
        callHistoryList = new ArrayList<>();
        userInfoMap = new HashMap<>();
        adapter = new CallHistoryAdapter(getContext(), callHistoryList, this);

        // Thiết lập RecyclerView
        rvCallHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCallHistory.setAdapter(adapter);

        // Khởi tạo API và SharedPreferences
        if (getContext() != null) {
            apiService = ApiClient.getAuthClient(getContext()).create(ApiService.class);
            prefManager = new SharedPrefManager(getContext());
        }

        // Tải dữ liệu
        loadCallHistory();
    }

    private void loadCallHistory() {
        Long userId = prefManager.getUser().getId();
        if (userId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        apiService.getCallHistory(userId).enqueue(new Callback<List<CallHistoryResponse>>() {
            @Override
            public void onResponse(Call<List<CallHistoryResponse>> call, Response<List<CallHistoryResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    callHistoryList.clear();
                    callHistoryList.addAll(response.body());
                    adapter.updateData(callHistoryList);

                    if (callHistoryList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        // Tải thông tin người dùng cho từng cuộc gọi
                        loadUserInfoForCalls();
                    }
                } else {
                    showError("Lỗi tải dữ liệu: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<CallHistoryResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Lỗi kết nối: " + t.getMessage());
                Log.e("CallHistory", "API Error", t);
            }
        });
    }

    private void loadUserInfoForCalls() {
        List<Long> userIds = adapter.getNeededUserIds();
        for (Long userId : userIds) {
            apiService.getUserInfo(userId).enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.addUserInfo(userId, response.body());
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    Log.e("CallHistory", "Error loading user info", t);
                }
            });
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCallClick(CallHistoryResponse callHistory) {
        // Xác định người liên hệ (người gọi hoặc người nhận)
        Long userId = prefManager.getUser().getId();
        Long contactId = callHistory.getCallerId().equals(userId)
                ? callHistory.getReceiverId() : callHistory.getCallerId();

        // Lấy thông tin người dùng từ adapter
        UserResponse contactInfo = null;
        if (callHistory.getCallerId().equals(userId)) {
            contactInfo = adapter.getUserInfo(callHistory.getReceiverId());
        } else {
            contactInfo = adapter.getUserInfo(callHistory.getCallerId());
        }

        String contactName = contactInfo != null ? contactInfo.getUsername() : "Unknown";
        String contactAvatar = contactInfo != null ? contactInfo.getAvatarURL() : null;

        // Mở ChatActivity với thông tin người liên hệ
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("receiverId", contactId);
        intent.putExtra("receiverName", contactName);
        intent.putExtra("receiverAvatar", contactAvatar);
        startActivity(intent);
    }
}