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

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.R;
import ute.nhom27.android.adapter.FriendAdapter;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.model.response.NotificationResponse;
import ute.nhom27.android.model.response.UserResponse;
import ute.nhom27.android.network.OnMessageReceivedListener;
import ute.nhom27.android.network.WebSocketClient;
import ute.nhom27.android.utils.SharedPrefManager;

public class FriendRequestFragment extends Fragment implements OnMessageReceivedListener {

    private static final String TAG = "FriendRequestFragment";
    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private ApiService apiService;
    private WebSocketClient webSocketClient;
    private List<UserResponse> friendRequests;

    public FriendRequestFragment() {}

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
        
        friendRequests = new ArrayList<>();
        adapter = new FriendAdapter(friendRequests, FriendAdapter.TYPE_FRIEND_REQUEST);
        adapter.setOnItemClickListener(user -> {
            Toast.makeText(getContext(), "Đã chọn: " + user.getUsername(), Toast.LENGTH_SHORT).show();
        });

        setupFriendRequestCallbacks();
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getAuthClient(requireContext()).create(ApiService.class);
        webSocketClient = new WebSocketClient(requireContext(), this);
        adapter.setWebSocketClient(webSocketClient);
        webSocketClient.connect();

        fetchFriendRequests();
    }

    private void setupFriendRequestCallbacks() {
        SharedPrefManager sharedPrefManager = new SharedPrefManager(requireContext());
        Long currentUserId = sharedPrefManager.getUser().getId();

        adapter.setOnAcceptClickListener((user, position) -> {
            apiService.acceptFriendRequest(currentUserId, user.getId())
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                adapter.removeItem(position);
                                adapter.sendWebSocketNotification(currentUserId, user.getId(), "FRIEND_ACCEPT");
                                Toast.makeText(getContext(), "Đã chấp nhận lời mời kết bạn", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Lỗi khi chấp nhận", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        adapter.setOnRejectClickListener((user, position) -> {
            apiService.rejectFriendRequest(currentUserId, user.getId())
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                adapter.removeItem(position);
                                adapter.sendWebSocketNotification(currentUserId, user.getId(), "FRIEND_REJECT");
                                Toast.makeText(getContext(), "Đã từ chối lời mời kết bạn", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Lỗi khi từ chối", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void fetchFriendRequests() {
        SharedPrefManager sharedPrefManager = new SharedPrefManager(requireContext());
        Long userId = sharedPrefManager.getUser().getId();
        apiService.getFriendRequests(userId).enqueue(new Callback<List<UserResponse>>() {
            @Override
            public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    friendRequests.clear();
                    friendRequests.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi lấy danh sách lời mời kết bạn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                Log.e(TAG, "Error fetching friend requests: " + t.getMessage());
                Toast.makeText(getContext(), "Không thể kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMessageReceived(String message) {
        // Không xử lý tin nhắn ở đây
    }

    @Override
    public void onNotificationReceived(NotificationResponse notification) {
        Log.d(TAG, "Notification received: type=" + notification.getType() + 
                   ", senderId=" + notification.getSenderId() + 
                   ", usernameSender=" + notification.getUsernameSender());
                   
        if ("FRIEND_REQUEST".equals(notification.getType())) {
            UserResponse user = new UserResponse();
            user.setId(notification.getSenderId());
            user.setUsername(notification.getUsernameSender() != null ? 
                           notification.getUsernameSender() : "Unknown");
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.addItem(user);
                    recyclerView.scrollToPosition(0);
                    Toast.makeText(getContext(), 
                        "Lời mời mới từ: " + user.getUsername(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    @Override
    public void onTypingStatusReceived(String typing) {
        // Không xử lý typing ở đây
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
    }

}