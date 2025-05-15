package ute.nhom27.android.view.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.R;
import ute.nhom27.android.adapter.AIChatAdapter;
import ute.nhom27.android.adapter.ChatAdapter;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.api.DeepAIRequest;
import ute.nhom27.android.api.DeepAIResponse;
import ute.nhom27.android.model.response.MessageResponse;
import ute.nhom27.android.utils.SharedPrefManager;

public class AIChatFragment extends Fragment {
    private static final long AI_USER_ID = -1L;
    private static final String OPENAI_API_KEY = "Ro3w2jTC7OuHIUxXiIJjwQEilC26WUiwXc27EW8j";
    private static final String AI_NAME = "AI Assistant";
    private static final String AI_AVATAR = "https://cdn-icons-png.flaticon.com/512/11865/11865338.png";

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvName, tvStatus;
    private AIChatAdapter chatAdapter;
    private ApiService apiService;
    private Long currentUserId;
    private List<MessageResponse> messageList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAIInfo(view);
        setupRecyclerView();
        setupServices();
        setupClickListeners(view);
    }

    private void initViews(View view) {
        rvMessages = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        tvName = view.findViewById(R.id.tvName);
        tvStatus = view.findViewById(R.id.tvStatus);

        // Ẩn các nút không cần thiết
        view.findViewById(R.id.btnAttachment).setVisibility(View.GONE);
        view.findViewById(R.id.btnEmoji).setVisibility(View.GONE);
        view.findViewById(R.id.btnVoice).setVisibility(View.GONE);
        view.findViewById(R.id.btnVoiceCall).setVisibility(View.GONE);
        view.findViewById(R.id.btnVideoCall).setVisibility(View.GONE);

        // Hiển thị nút gửi tin nhắn
        btnSend.setVisibility(View.VISIBLE);

        // Thêm TextWatcher để hiển thị/ẩn nút gửi dựa trên nội dung tin nhắn
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSend.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupAIInfo(View view) {
        tvName.setText(AI_NAME);
        tvStatus.setText("Online");

        // Sửa lỗi Glide ambiguous method call
        ImageView ivAvatar = view.findViewById(R.id.ivAvatar);
        Glide.with(this)
                .load(AI_AVATAR)
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(ivAvatar);
    }

    private void setupServices() {
        apiService = ApiClient.getOpenAiClient(OPENAI_API_KEY).create(ApiService.class);
        SharedPrefManager prefManager = new SharedPrefManager(requireContext());
        currentUserId = prefManager.getUser().getId();
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new AIChatAdapter(messageList, currentUserId);
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessages.setAdapter(chatAdapter);
    }

    private void setupClickListeners(View view) {
        btnSend.setOnClickListener(v -> sendMessage());
        view.findViewById(R.id.ivBack).setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        // Tạo tin nhắn người dùng
        MessageResponse userMessage = new MessageResponse();
        userMessage.setSenderId(currentUserId);
        userMessage.setReceiverId(AI_USER_ID);
        userMessage.setContent(messageText);
        userMessage.setStatus("SENT");
        userMessage.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        messageList.add(userMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        etMessage.setText("");
        rvMessages.smoothScrollToPosition(messageList.size() - 1);

        // Thêm log để debug
        Log.d("AIChat", "Sending message to AI: " + messageText);

        try {
            DeepAIRequest request = new DeepAIRequest(messageText);

            Log.d("AIChat", "Request: " + new Gson().toJson(request));
            Log.d("AIChat", "API Key: " + OPENAI_API_KEY);

            apiService.getChatResponse(OPENAI_API_KEY, request)  // Bỏ "Bearer " prefix
                    .enqueue(new Callback<DeepAIResponse>() {
                        @Override
                        public void onResponse(Call<DeepAIResponse> call, Response<DeepAIResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String aiResponse = response.body().getResponse();
                                Log.d("AIChat", "Received AI response: " + aiResponse);
                                sendAIMessage(aiResponse);
                            } else {
                                String errorBody = "";
                                try {
                                    if (response.errorBody() != null) {
                                        errorBody = response.errorBody().string();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Log.e("AIChat", "Error response: " + response.code() + " - " + errorBody);
                                showError("Không thể nhận phản hồi từ AI: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<DeepAIResponse> call, Throwable t) {
                            Log.e("AIChat", "API call failed", t);
                            showError("Lỗi kết nối: " + t.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e("AIChat", "Error creating request", e);
            showError("Lỗi tạo request: " + e.getMessage());
        }
    }

    private void sendAIMessage(String content) {
        MessageResponse aiMessage = new MessageResponse();
        aiMessage.setSenderId(AI_USER_ID); // ID của AI
        aiMessage.setReceiverId(currentUserId);
        aiMessage.setContent(content);
        aiMessage.setStatus("SENT");
        aiMessage.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        requireActivity().runOnUiThread(() -> {
            messageList.add(aiMessage);
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            rvMessages.smoothScrollToPosition(messageList.size() - 1);
        });
    }

    private void showError(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
        );
    }
}