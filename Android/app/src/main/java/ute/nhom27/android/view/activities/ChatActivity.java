package ute.nhom27.android.view.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.BaseActivity;
import ute.nhom27.android.R;
import ute.nhom27.android.adapter.ChatAdapter;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.model.request.MessageRequest;
import ute.nhom27.android.model.response.MessageResponse;
import ute.nhom27.android.model.response.NotificationResponse;
import ute.nhom27.android.network.OnMessageReceivedListener;
import ute.nhom27.android.network.WebSocketClient;
import ute.nhom27.android.utils.CloudinaryUtils;
import ute.nhom27.android.utils.SharedPrefManager;

public class ChatActivity extends BaseActivity implements OnMessageReceivedListener {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int POLLING_INTERVAL = 3000; // 3 seconds

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend, btnAttachment, btnEmoji, btnVoice;
    private ImageView ivAvatar;
    private TextView tvName;
    private ZegoSendCallInvitationButton btnVoiceCall, btnVideoCall;
    private LinearLayout layoutAttachment;

    private LinearLayout layoutPreview;
    private ImageView ivPreview;
    private ImageButton btnCancelPreview;
    private Uri selectedImageUri;
    private Bitmap capturedImage;

    private ChatAdapter chatAdapter;
    private List<MessageResponse> messageList;
    private Long receiverId;
    private String receiverName, receiverAvatar;
    private Long currentUserId;
    private ApiService apiService;
    private Handler handler;
    private SharedPrefManager prefManager;
    private WebSocketClient webSocketClient;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        CloudinaryUtils.init(this);

        messageList = new ArrayList<>();

        // Lấy dữ liệu từ Intent
        receiverId = getIntent().getLongExtra("receiverId", -1);
        receiverName = getIntent().getStringExtra("receiverName");
        receiverAvatar = getIntent().getStringExtra("receiverAvatar");

        // Lấy currentUserId từ SharedPreferences
        prefManager = new SharedPrefManager(this);
        currentUserId = prefManager.getUser().getId();

        if (currentUserId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo ApiService và WebSocket
        apiService = ApiClient.getAuthClient(this).create(ApiService.class);
        webSocketClient = new WebSocketClient(this, this);
        webSocketClient.connect();

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

        btnVoiceCall.setIsVideoCall(false);
        btnVoiceCall.setResourceID("zego_uikit_call");
        btnVoiceCall.setInvitees(Collections.singletonList(new ZegoUIKitUser(receiverName)));

        btnVideoCall.setIsVideoCall(true);
        btnVideoCall.setResourceID("zego_uikit_call");
        btnVideoCall.setInvitees(Collections.singletonList(new ZegoUIKitUser(receiverName)));

        layoutPreview = findViewById(R.id.layoutPreview);
        ivPreview = findViewById(R.id.ivPreview);
        btnCancelPreview = findViewById(R.id.btnCancelPreview);

        btnCancelPreview.setOnClickListener(v -> {
            layoutPreview.setVisibility(View.GONE);
            selectedImageUri = null;
            capturedImage = null;
            // Ẩn nút gửi nếu không có text
            if (etMessage.getText().toString().trim().isEmpty()) {
                btnSend.setVisibility(View.GONE);
                btnVoice.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messageList, currentUserId, receiverName);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
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

    @Override
    public void onMessageReceived(String message) {
        try {
            JSONObject jsonMessage = new JSONObject(message);
            String type = jsonMessage.getString("type");
            if ("MESSAGE".equals(type)) {
                // Xử lý tin nhắn mới
                loadMessages(); // Tải lại danh sách tin nhắn
            }
        } catch (JSONException e) {
            Log.e("ChatActivity", "Error parsing message: " + e.getMessage());
        }
    }

    @Override
    public void onNotificationReceived(NotificationResponse notification) {
        // Xử lý thông báo nếu cần
        if ("MESSAGE".equals(notification.getType())) {
            runOnUiThread(this::loadMessages);
        }
    }

    @Override
    public void onTypingStatusReceived(String typing) {
        try {
            JSONObject jsonTyping = new JSONObject(typing);
            Long senderIdTyping = jsonTyping.getLong("senderId");
            if (senderIdTyping.equals(receiverId)) {
                runOnUiThread(() -> {
                    // Hiển thị trạng thái đang nhập
                    tvName.setText(receiverName + " đang nhập...");
                    // Sau 2 giây, đặt lại tên
                    new Handler().postDelayed(() -> 
                        tvName.setText(receiverName), 2000);
                });
            }
        } catch (JSONException e) {
            Log.e("ChatActivity", "Error parsing typing status: " + e.getMessage());
        }
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected) {
        this.isConnected = isConnected;
        runOnUiThread(() -> {
            if (!isConnected) {
                Toast.makeText(this, "Mất kết nối với server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        // Kiểm tra xem có ảnh xem trước không
        if (layoutPreview.getVisibility() == View.VISIBLE && (selectedImageUri != null || capturedImage != null)) {
            // Upload ảnh lên Cloudinary
            if (selectedImageUri != null) {
                CloudinaryUtils.uploadImage(this, selectedImageUri, new CloudinaryUtils.UploadCallback() {
                    @Override
                    public void onSuccess(String url) {
                        sendImageMessage(url);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ChatActivity.this,
                                "Lỗi gửi tin nhắn: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (capturedImage != null) {
                CloudinaryUtils.uploadImage(this, capturedImage, new CloudinaryUtils.UploadCallback() {
                    @Override
                    public void onSuccess(String url) {
                        sendImageMessage(url);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ChatActivity.this,
                                "Lỗi gửi tin nhắn: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            // Gửi tin nhắn text bình thường
            String messageText = etMessage.getText().toString().trim();
            if (messageText.isEmpty()) return;

            MessageRequest message = new MessageRequest();
            message.setSenderId(currentUserId);
            message.setReceiverId(receiverId);
            message.setContent(messageText);
            message.setStatus("SENT");
            message.setGroup(false);
            message.setTimestamp(LocalDateTime.now().toString());

            apiService.sendPrivateMessage(message)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                etMessage.setText("");
                                // Thêm tin nhắn vào danh sách local
                                MessageResponse newMessage = new MessageResponse();
                                newMessage.setSenderId(currentUserId);
                                newMessage.setReceiverId(receiverId);
                                newMessage.setContent(messageText);
                                newMessage.setTimestamp(message.getTimestamp());
                                messageList.add(newMessage);
                                runOnUiThread(() -> {
                                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                                    rvMessages.scrollToPosition(messageList.size() - 1);
                                });
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(ChatActivity.this,
                                            "Lỗi gửi tin nhắn: " + response.code(),
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            runOnUiThread(() -> {
                                Toast.makeText(ChatActivity.this,
                                        "Lỗi kết nối: " + t.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    if (data != null && data.getExtras() != null) {
                        capturedImage = (Bitmap) data.getExtras().get("data");
                        // Hiển thị ảnh xem trước
                        ivPreview.setImageBitmap(capturedImage);
                        layoutPreview.setVisibility(View.VISIBLE);
                        layoutAttachment.setVisibility(View.GONE);
                        // Hiển thị nút gửi
                        btnSend.setVisibility(View.VISIBLE);
                        btnVoice.setVisibility(View.GONE);
                    }
                    break;
                case REQUEST_PICK_IMAGE:
                    if (data != null && data.getData() != null) {
                        selectedImageUri = data.getData();
                        // Hiển thị ảnh xem trước
                        Glide.with(this)
                                .load(selectedImageUri)
                                .into(ivPreview);
                        layoutPreview.setVisibility(View.VISIBLE);
                        layoutAttachment.setVisibility(View.GONE);
                        // Hiển thị nút gửi
                        btnSend.setVisibility(View.VISIBLE);
                        btnVoice.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    }
    private void sendImageMessage(String imageUrl) {
        MessageRequest message = new MessageRequest();
        message.setSenderId(currentUserId);
        message.setReceiverId(receiverId);
        message.setMediaType("IMAGE");
        message.setMediaUrl(imageUrl);
        message.setStatus("SENT");
        message.setGroup(false);
        message.setTimestamp(LocalDateTime.now().toString());

        apiService.sendPrivateMessage(message)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            // Reset trạng thái xem trước
                            runOnUiThread(() -> {
                                layoutPreview.setVisibility(View.GONE);
                                selectedImageUri = null;
                                capturedImage = null;
                                btnSend.setVisibility(View.GONE);
                                btnVoice.setVisibility(View.VISIBLE);

                                // Thêm tin nhắn vào danh sách
                                MessageResponse newMessage = new MessageResponse();
                                newMessage.setSenderId(currentUserId);
                                newMessage.setReceiverId(receiverId);
                                newMessage.setMediaType("IMAGE");
                                newMessage.setMediaUrl(imageUrl);
                                newMessage.setTimestamp(message.getTimestamp());
                                messageList.add(newMessage);
                                chatAdapter.notifyItemInserted(messageList.size() - 1);
                                rvMessages.scrollToPosition(messageList.size() - 1);
                            });
                        } else {
                            Toast.makeText(ChatActivity.this,
                                    "Lỗi gửi tin nhắn: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(ChatActivity.this,
                                "Lỗi gửi tin nhắn: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
