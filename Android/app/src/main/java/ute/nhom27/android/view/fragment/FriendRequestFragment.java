package ute.nhom27.android.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.R;
import ute.nhom27.android.adapter.FriendRequestAdapter;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.model.response.UserResponse;  // giả sử dữ liệu trả về là kiểu UserResponse
import ute.nhom27.android.utils.SharedPrefManager;

public class FriendRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;
    private ApiService apiService;


    public FriendRequestFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_friend_requests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        apiService = ApiClient.getAuthClient(requireContext()).create(ApiService.class);

        fetchFriendRequests();
    }

    private void fetchFriendRequests() {
        SharedPrefManager sharedPrefManager = new SharedPrefManager(requireContext());
        // Cập nhật userId theo người dùng đăng nhập
        Long userId = sharedPrefManager.getUser().getId();
        // Giả sử ApiService có phương thức getFriendRequests(userId)
        apiService.getFriendRequests(userId).enqueue(new Callback<List<UserResponse>>() {
            @Override
            public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserResponse> requests = response.body();
                    adapter = new FriendRequestAdapter(requests, getContext(), user -> {
                        // Xử lý khi người dùng click vào một lời mời: Ví dụ mở chi tiết hoặc thực hiện hành động.
                        Toast.makeText(getContext(), "Đã chọn: " + user.getUsername(), Toast.LENGTH_SHORT).show();
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "Lỗi khi lấy danh sách lời mời kết bạn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                Log.e("FriendRequest", t.getMessage());
                Toast.makeText(getContext(), "Không thể kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}