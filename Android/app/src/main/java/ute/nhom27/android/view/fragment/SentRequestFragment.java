package ute.nhom27.android.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class SentRequestFragment extends Fragment implements OnMessageReceivedListener {
    private static final String TAG = "SentRequestFragment";
    
    private RecyclerView rvSentRequests;
    private RecyclerView rvSuggestions;
    private FriendAdapter sentRequestsAdapter;
    private FriendAdapter suggestionsAdapter;
    private TextView tvNoSentRequests;
    private TextView tvNoSuggestions;
    private ApiService apiService;
    private Long currentUserId;
    private Set<Long> processingUsers;
    private WebSocketClient webSocketClient;

    public SentRequestFragment() {
        processingUsers = new HashSet<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sent_request, container, false);

        rvSentRequests = view.findViewById(R.id.rv_sent_requests);
        rvSuggestions = view.findViewById(R.id.rv_suggestions);
        tvNoSentRequests = view.findViewById(R.id.tv_no_sent_requests);
        tvNoSuggestions = view.findViewById(R.id.tv_no_suggestions);

        apiService = ApiClient.getAuthClient(requireContext()).create(ApiService.class);
        SharedPrefManager sharedPrefManager = new SharedPrefManager(requireContext());
        currentUserId = sharedPrefManager.getUser().getId();

        webSocketClient = new WebSocketClient(requireContext(), this);
        webSocketClient.connect();

        setupRecyclerViews();
        fetchData();

        return view;
    }

    private void setupRecyclerViews() {
        // Setup for sent requests
        sentRequestsAdapter = new FriendAdapter(new ArrayList<>(), FriendAdapter.TYPE_SENT_REQUEST);
        rvSentRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSentRequests.setAdapter(sentRequestsAdapter);
        sentRequestsAdapter.setWebSocketClient(webSocketClient);

        // Setup for suggestions
        suggestionsAdapter = new FriendAdapter(new ArrayList<>(), FriendAdapter.TYPE_SUGGESTION);
        rvSuggestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSuggestions.setAdapter(suggestionsAdapter);
        suggestionsAdapter.setWebSocketClient(webSocketClient);

        // Set callbacks for buttons
        sentRequestsAdapter.setOnWithdrawClickListener((user, position) -> {
            if (!processingUsers.contains(user.getId())) {
                withdrawFriendRequest(user, position);
            }
        });

        suggestionsAdapter.setOnAddFriendClickListener((user, position) -> {
            if (!processingUsers.contains(user.getId())) {
                sendFriendRequest(user, position);
            }
        });
    }

    private void fetchData() {
        fetchSentRequests();
        fetchSuggestions();
    }

    private void fetchSentRequests() {
        apiService.getSentFriendRequests(currentUserId).enqueue(new Callback<List<UserResponse>>() {
            @Override
            public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Sent requests fetched: " + response.body().size());
                    List<UserResponse> filteredRequests = new ArrayList<>();
                    for (UserResponse user : response.body()) {
                        if (!processingUsers.contains(user.getId())) {
                            filteredRequests.add(user);
                        }
                    }
                    sentRequestsAdapter.updateData(filteredRequests);
                    checkEmptyViews();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi lấy danh sách lời mời đã gửi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                Log.e(TAG, "Error fetching sent requests", t);
                Toast.makeText(getContext(), "Không thể kết nối tới máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSuggestions() {
        apiService.getNonFriendUsers(currentUserId).enqueue(new Callback<List<UserResponse>>() {
            @Override
            public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Suggestions fetched: " + response.body().size());
                    List<UserResponse> filteredSuggestions = new ArrayList<>();
                    for (UserResponse user : response.body()) {
                        if (!processingUsers.contains(user.getId())) {
                            filteredSuggestions.add(user);
                        }
                    }
                    suggestionsAdapter.updateData(filteredSuggestions);
                    checkEmptyViews();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi lấy danh sách gợi ý", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                Log.e(TAG, "Error fetching suggestions", t);
                Toast.makeText(getContext(), "Không thể kết nối tới máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void withdrawFriendRequest(UserResponse user, int position) {
        processingUsers.add(user.getId());
        apiService.removeFriendRequest(currentUserId, user.getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            sentRequestsAdapter.removeItem(position);
                            sentRequestsAdapter.sendWebSocketNotification(currentUserId, user.getId(), "FRIEND_REQUEST_WITHDRAWN");
                            checkEmptyViews();
                            Toast.makeText(getContext(), "Đã thu hồi lời mời kết bạn", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Không thể thu hồi lời mời", Toast.LENGTH_SHORT).show();
                        }
                        processingUsers.remove(user.getId());
                        fetchData(); // Refresh both lists after processing
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "Error withdrawing request", t);
                        Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        processingUsers.remove(user.getId());
                    }
                });
    }

    private void sendFriendRequest(UserResponse user, int position) {
        processingUsers.add(user.getId());
        apiService.sendFriendRequest(currentUserId, user.getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            suggestionsAdapter.removeItem(position);
                            suggestionsAdapter.sendWebSocketNotification(currentUserId, user.getId(), "FRIEND_REQUEST");
                            checkEmptyViews();
                            Toast.makeText(getContext(), "Đã gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Không thể gửi lời mời", Toast.LENGTH_SHORT).show();
                        }
                        processingUsers.remove(user.getId());
                        fetchData(); // Refresh both lists after processing
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "Error sending request", t);
                        Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        processingUsers.remove(user.getId());
                    }
                });
    }

    private void checkEmptyViews() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                tvNoSentRequests.setVisibility(sentRequestsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                tvNoSuggestions.setVisibility(suggestionsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            });
        }
    }

    @Override
    public void onMessageReceived(String message) {
        // Không xử lý tin nhắn ở đây
    }

    @Override
    public void onNotificationReceived(NotificationResponse notification) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if ("FRIEND_REQUEST_WITHDRAWN".equals(notification.getType()) ||
                    "FRIEND_REQUEST".equals(notification.getType())) {
                    fetchData(); // Refresh both lists when receiving relevant notifications
                }
            });
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
                if (isConnected) {
                    fetchData(); // Refresh data when reconnected
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
        processingUsers.clear();
    }
} 