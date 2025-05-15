package ute.nhom27.android.view.fragment;

import android.content.Intent;
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
import ute.nhom27.android.adapter.MessageListAdapter;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.model.response.MessageListResponse;
import ute.nhom27.android.utils.SharedPrefManager;
import ute.nhom27.android.view.activities.ChatActivity;

public class FriendMessagesFragment extends Fragment {
    private RecyclerView recyclerView;
    private MessageListAdapter adapter;
    private ApiService apiService;

    public FriendMessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_friend_chats);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        apiService = ApiClient.getAuthClient(requireContext()).create(ApiService.class);

        fetchFriendMessages();
    }

    private void fetchFriendMessages() {
        SharedPrefManager sharedPrefManager = new SharedPrefManager(requireContext());
        Long userId = sharedPrefManager.getUser().getId();

        Log.d("MessageListFragment", "Fetching messages for userId: " + userId);

        apiService.getFriendLastMessages(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<MessageListResponse>> call, Response<List<MessageListResponse>> response) {
                Log.d("MessageListFragment", "Response code: " + response.code());

                if (response.isSuccessful()) {
                    List<MessageListResponse> list = response.body();
                    if (list == null) {
                        Log.d("MessageListFragment", "Response body is null");
                        list = new ArrayList<>();
                    }
                    adapter = new MessageListAdapter(list, getContext());
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "Lỗi dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MessageListResponse>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(getContext(), "Không thể kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 