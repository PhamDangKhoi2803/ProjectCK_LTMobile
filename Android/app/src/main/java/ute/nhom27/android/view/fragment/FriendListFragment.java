package ute.nhom27.android.view.fragment;

import android.app.AlertDialog;
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

public class FriendListFragment extends Fragment implements OnMessageReceivedListener {

    private static final String TAG = "FriendListFragment";
    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private ApiService apiService;
    private WebSocketClient webSocketClient;
    private List<UserResponse> friendList;

    public FriendListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_friends);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        apiService = ApiClient.getAuthClient(requireContext()).create(ApiService.class);

        friendList = new ArrayList<>();
        adapter = new FriendAdapter(friendList, FriendAdapter.TYPE_FRIEND_LIST);
        
        setupFriendListCallbacks();
        recyclerView.setAdapter(adapter);

        webSocketClient = new WebSocketClient(requireContext(), this);
        adapter.setWebSocketClient(webSocketClient);
        webSocketClient.connect();

        fetchFriends();

        return view;
    }

    private void setupFriendListCallbacks() {
        SharedPrefManager sharedPrefManager = new SharedPrefManager(requireContext());
        Long currentUserId = sharedPrefManager.getUser().getId();

        adapter.setOnMessageClickListener(user -> {
            // TODO: Mở màn hình chat với user
            Toast.makeText(getContext(), "Bắt đầu chat với " + user.getUsername(), Toast.LENGTH_SHORT).show();
        });

        adapter.setOnUnfriendClickListener((user, position) -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận hủy kết bạn")
                    .setMessage("Bạn có chắc chắn muốn hủy kết bạn với " + user.getUsername() + "?")
                    .setPositiveButton("Hủy kết bạn", (dialog, which) -> {
                        // TODO: Implement unfriend API call
                        apiService.unfriend(currentUserId, user.getId()).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    adapter.removeItem(position);
                                    adapter.sendWebSocketNotification(currentUserId, user.getId(), "UNFRIEND");
                                    Toast.makeText(getContext(), 
                                        "Đã hủy kết bạn với " + user.getUsername(), 
                                        Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), 
                                        "Không thể hủy kết bạn. Vui lòng thử lại sau", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(getContext(), 
                                    "Lỗi kết nối: " + t.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void fetchFriends() {
        SharedPrefManager sharedPrefManager = new SharedPrefManager(requireContext());
        Long userId = sharedPrefManager.getUser().getId();
        apiService.getFriends(userId).enqueue(new Callback<List<UserResponse>>() {
            @Override
            public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    friendList.clear();
                    friendList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi lấy danh sách bạn bè", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                Log.e(TAG, "Error fetching friends: " + t.getMessage());
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
        if ("UNFRIEND".equals(notification.getType())) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::fetchFriends);
            }
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
                Toast.makeText(getContext(), 
                    isConnected ? "WebSocket Connected" : "WebSocket Disconnected", 
                    Toast.LENGTH_SHORT).show();
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
