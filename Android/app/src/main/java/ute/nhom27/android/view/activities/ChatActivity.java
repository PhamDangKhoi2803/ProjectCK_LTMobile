package ute.nhom27.android.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.BaseActivity;
import ute.nhom27.android.R;
import ute.nhom27.android.adapter.ChatAdapter;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.model.ChatMessage;
import ute.nhom27.android.model.User;
import ute.nhom27.android.model.response.MessageResponse;
import ute.nhom27.android.utils.SharedPrefManager;

public class ChatActivity extends BaseActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int POLLING_INTERVAL = 3000; // 3 seconds

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend, btnAttachment, btnEmoji, btnVoice;
    private ImageView ivAvatar;
    private TextView tvName, tvStatus;
    private ImageButton btnVoiceCall, btnVideoCall;
    private LinearLayout layoutAttachment;

    private ChatAdapter chatAdapter;
    private List<MessageResponse> messageList;
    private Long receiverId;
    private String receiverName, receiverAvatar;
    private Long currentUserId;
    private ApiService apiService;
    private Handler handler;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        messageList = new ArrayList<>();


        // Lấy dữ liệu từ Intent
        receiverId = getIntent().getLongExtra("receiverId", -1);
        receiverName = getIntent().getStringExtra("receiverName");
        receiverAvatar = getIntent().getStringExtra("receiverAvatar");

        Log.d("ChatActivity", "Received ID: " + receiverId);
        Log.d("ChatActivity", "Received Name: " + receiverName);
        Log.d("ChatActivity", "Received Avatar: " + receiverAvatar);

        // Lấy currentUserId từ SharedPreferences
        prefManager = new SharedPrefManager(this);
        currentUserId = prefManager.getUser().getId();

        if (currentUserId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo ApiService
        apiService = ApiClient.getAuthClient(this).create(ApiService.class);

        handler = new Handler();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadMessages();
        startPolling();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnAttachment = findViewById(R.id.btnAttachment);
        btnEmoji = findViewById(R.id.btnEmoji);
        btnVoice = findViewById(R.id.btnVoice);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvName = findViewById(R.id.tvName);
        tvStatus = findViewById(R.id.tvStatus);
        btnVoiceCall = findViewById(R.id.btnVoiceCall);
        btnVideoCall = findViewById(R.id.btnVideoCall);
        layoutAttachment = findViewById(R.id.layoutAttachment);

        // Set receiver info
        tvName.setText(receiverName);
        Glide.with(this)
                .load(receiverAvatar)
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(ivAvatar);
    }

    private void setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
    }

    //    private void setupRecyclerView() {
//        messageList = new ArrayList<>();
//        chatAdapter = new ChatAdapter(messageList, currentUserId, this);
//
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setStackFromEnd(true);
//
//        rvMessages.setLayoutManager(layoutManager);
//        rvMessages.setAdapter(chatAdapter);
//    }
    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messageList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Thêm dòng này để tin nhắn mới nhất hiển thị ở dưới
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(chatAdapter);
    }

    private void setupClickListeners() {
        // Nút gửi tin nhắn
        btnSend.setOnClickListener(v -> sendMessage());

        // Nút đính kèm
        btnAttachment.setOnClickListener(v -> {
            if (layoutAttachment.getVisibility() == View.VISIBLE) {
                layoutAttachment.setVisibility(View.GONE);
            } else {
                layoutAttachment.setVisibility(View.VISIBLE);
            }
        });

        // Nút emoji
        btnEmoji.setOnClickListener(v -> {
            // TODO: Implement emoji picker
        });

        // Nút ghi âm
        btnVoice.setOnClickListener(v -> {
            // TODO: Implement voice recording
        });

        // Nút gọi thoại
        btnVoiceCall.setOnClickListener(v -> {
            // TODO: Implement voice call
        });

        // Nút gọi video
        btnVideoCall.setOnClickListener(v -> {
            // TODO: Implement video call
        });

        // Các nút trong menu đính kèm
        findViewById(R.id.btnGallery).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        });

        findViewById(R.id.btnCamera).setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        });

        findViewById(R.id.btnFile).setOnClickListener(v -> {
            // TODO: Implement file picker
        });

        findViewById(R.id.btnLocation).setOnClickListener(v -> {
            // TODO: Implement location picker
        });

        // Hiển thị/ẩn nút gửi khi có text
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSend.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                btnVoice.setVisibility(s.length() > 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadMessages() {
        if (currentUserId == null || receiverId == null) {
            Log.e("ChatActivity", "currentUserId or receiverId is null");
            return;
        }

        String token = prefManager.getToken();
        Log.d("ChatActivity", "Token: " + token);
        Log.d("ChatActivity", "Loading messages between " + currentUserId + " and " + receiverId);

        apiService.getPrivateMessages(currentUserId, receiverId).enqueue(new Callback<List<MessageResponse>>() {
            @Override
            public void onResponse(Call<List<MessageResponse>> call, Response<List<MessageResponse>> response) {
                Log.d("ChatActivity", "Response code: " + response.code());
                Log.d("ChatActivity", "Response headers: " + response.headers());

                if (response.isSuccessful() && response.body() != null) {
                    List<MessageResponse> messages = response.body();
                    Log.d("ChatActivity", "Total messages received: " + messages.size());

                    runOnUiThread(() -> {
                        messageList.clear();
                        messageList.addAll(messages);
                        chatAdapter.notifyDataSetChanged();

                        if (!messageList.isEmpty()) {
                            rvMessages.scrollToPosition(messageList.size() - 1);
                        }
                    });
                } else {
                    Log.e("ChatActivity", "Error response: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "No error body";
                        Log.e("ChatActivity", "Error body: " + errorBody);

                        Log.e("ChatActivity", "Request URL: " + call.request().url());
                        Log.e("ChatActivity", "Request headers: " + call.request().headers());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<MessageResponse>> call, Throwable t) {
                Log.e("ChatActivity", "API call failed", t);
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this,
                            "Không thể kết nối server: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void startPolling() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadMessages();
                handler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        ChatMessage message = new ChatMessage();
        User sender = new User();
        sender.setId(currentUserId);
        User receiver = new User();
        receiver.setId(receiverId);

        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(messageText);
        message.setStatus("SENT");
        // Sử dụng LocalDateTime thay vì SimpleDateFormat
        LocalDateTime now = LocalDateTime.now();
        message.setTimestamp(now.toString()); // Sẽ tự động format theo ISO-8601

        apiService.sendMessage(message)
                .enqueue(new Callback<ChatMessage>() {
                    @Override
                    public void onResponse(Call<ChatMessage> call, Response<ChatMessage> response) {
                        if (response.isSuccessful()) {
                            etMessage.setText("");
                            loadMessages();
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatMessage> call, Throwable t) {
                        Toast.makeText(ChatActivity.this,
                                "Error sending message: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    if (data != null && data.getExtras() != null) {
                        Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                        // Convert bitmap to file and send
                        // TODO: Implement this
                    }
                    break;
                case REQUEST_PICK_IMAGE:
                    if (data != null && data.getData() != null) {
                        Uri imageUri = data.getData();
                        // TODO: Implement image upload
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
