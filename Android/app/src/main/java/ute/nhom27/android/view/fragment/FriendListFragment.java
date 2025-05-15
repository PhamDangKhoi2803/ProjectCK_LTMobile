package ute.nhom27.android.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.R;
import ute.nhom27.android.adapter.FriendListAdapter;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.model.response.UserResponse;
import ute.nhom27.android.utils.SharedPrefManager;
import ute.nhom27.android.view.activities.ChatActivity;

public class FriendListFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendListAdapter adapter;
    private ApiService apiService;

    public FriendListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_friends);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        apiService = ApiClient.getAuthClient(requireContext()).create(ApiService.class);

        ImageButton btnAdd = view.findViewById(R.id.btn_add_friend);
        btnAdd.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Thêm bạn bè", Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình thêm bạn bè
        });

        fetchFriends();

        return view;
    }

    private void fetchFriends() {
        SharedPrefManager sharedPrefManager = new SharedPrefManager(requireContext());
        // Cập nhật userId theo người dùng đăng nhập
        Long userId = sharedPrefManager.getUser().getId();
        apiService.getFriends(userId).enqueue(new Callback<List<UserResponse>>() {
            @Override
            public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
                if (response.isSuccessful()) {
                    Log.e("FriendList", "Body: " + response.body());
                    List<UserResponse> friends = response.body();
                    if (friends == null) {
                        friends = new ArrayList<>();
                    }
                    adapter = new FriendListAdapter(friends, friend -> {
                        Toast.makeText(getContext(), "Đã chọn: " + friend.getUsername(), Toast.LENGTH_SHORT).show();
                        // Log để kiểm tra giá trị
                        Log.d("FriendListFragment", "Friend ID: " + friend.getId());
                        Log.d("FriendListFragment", "Friend Name: " + friend.getUsername());
                        Log.d("FriendListFragment", "Friend Avatar: " + friend.getAvatarURL());
                        // TODO: mở màn hình chat riêng hoặc thông tin bạn
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("receiverId", friend.getId());
                        intent.putExtra("receiverName", friend.getUsername());
                        intent.putExtra("receiverAvatar", friend.getAvatarURL());
                        startActivity(intent);
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "Lỗi khi lấy danh sách bạn bè", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                Log.e("FriendList", "API error", t);
                Toast.makeText(getContext(), "Không thể kết nối tới máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
