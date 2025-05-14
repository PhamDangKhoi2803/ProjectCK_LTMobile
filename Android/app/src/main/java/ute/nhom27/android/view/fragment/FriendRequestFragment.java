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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.R;
import ute.nhom27.android.adapter.FriendRequestAdapter;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.model.response.NotificationResponse;
import ute.nhom27.android.model.response.UserResponse;
import ute.nhom27.android.network.OnMessageReceivedListener;
import ute.nhom27.android.network.WebSocketClient;
import ute.nhom27.android.utils.SharedPrefManager;

public class FriendRequestFragment extends Fragment implements OnMessageReceivedListener {

    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;
    private ApiService apiService;
    private WebSocketClient webSocketClient;
    private List<UserResponse> friendRequests;

    public FriendRequestFragment() {
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
        friendRequests = new ArrayList<>();
        adapter = new FriendRequestAdapter(friendRequests, getContext(), webSocketClient, user -> {
            Toast.makeText(getContext(), "Đã chọn: " + user.getUsername(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getAuthClient(requireContext()).create(ApiService.class);

        webSocketClient = new WebSocketClient(requireContext(), this);
        webSocketClient.connect();

        fetchFriendRequests();
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
                Log.e("FriendRequest", "Error fetching friend requests: " + t.getMessage());
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
        Log.d("FriendRequest", "Notification received: type=" + notification.getType() + ", senderId=" + notification.getSenderId() + ", usernameSender=" + notification.getUsernameSender());
        if ("FRIEND_REQUEST".equals(notification.getType())) {
            UserResponse user = new UserResponse();
            user.setId(notification.getSenderId());
            user.setUsername(notification.getUsernameSender() != null ? notification.getUsernameSender() : "Unknown");
            friendRequests.add(0, user);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                    Toast.makeText(getContext(), "Lời mời mới từ: " + user.getUsername(), Toast.LENGTH_SHORT).show();
                });
            }
        } else if ("FRIEND_ACCEPT".equals(notification.getType())) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), notification.getUsernameSender() + " đã chấp nhận lời mời của bạn", Toast.LENGTH_SHORT).show();
                });
            }
        } else if ("FRIEND_REJECT".equals(notification.getType())) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), notification.getUsernameSender() + " đã từ chối lời mời của bạn", Toast.LENGTH_SHORT).show();
                });
            }
        } else {
            Log.d("FriendRequest", "Ignored notification type: " + notification.getType());
        }
    }

    @Override
    public void onTypingStatusReceived(String typing) {
        // Không xử lý typing ở đây
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), isConnected ? "WebSocket Connected" : "WebSocket Disconnected", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
    }
}