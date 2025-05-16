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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.BaseActivity;
import ute.nhom27.android.R;
import ute.nhom27.android.adapter.GroupChatAdapter;
import ute.nhom27.android.adapter.GroupMemberAdapter;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.model.request.MessageRequest;
import ute.nhom27.android.model.response.GroupMemberResponse;
import ute.nhom27.android.model.response.GroupMessageResponse;
import ute.nhom27.android.model.response.NotificationResponse;
import ute.nhom27.android.network.OnMessageReceivedListener;
import ute.nhom27.android.network.WebSocketClient;
import ute.nhom27.android.utils.CloudinaryUtils;
import ute.nhom27.android.utils.SharedPrefManager;

public class GroupChatActivity extends BaseActivity implements OnMessageReceivedListener {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;

    private static final int POLLING_INTERVAL = 3000;

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend, btnAttachment;
    private ImageView ivGroupAvatar;
    private TextView tvGroupName, tvMemberCount;
    private LinearLayout layoutAttachment;

    private LinearLayout layoutPreview;
    private ImageView ivPreview;
    private ImageButton btnCancelPreview;
    private Uri selectedImageUri;
    private Bitmap capturedImage;

    private ZegoSendCallInvitationButton btnVoiceCall, btnVideoCall;

    private GroupChatAdapter chatAdapter;
    private List<GroupMessageResponse> messageList;
    private Long groupId;
    private String groupName, groupAvatar;
    private Long currentUserId;
    private ApiService apiService;
    private Handler handler;
    private SharedPrefManager prefManager;
    private WebSocketClient webSocketClient;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        messageList = new ArrayList<>();

        // Lấy dữ liệu từ Intent
        groupId = getIntent().getLongExtra("groupId", -1);
        groupName = getIntent().getStringExtra("groupName");
        groupAvatar = getIntent().getStringExtra("groupAvatar");

        prefManager = new SharedPrefManager(this);
        currentUserId = prefManager.getUser().getId();

        if (currentUserId == null || groupId == -1) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
        ivGroupAvatar = findViewById(R.id.ivGroupAvatar);
        tvGroupName = findViewById(R.id.tvGroupName);
        tvMemberCount = findViewById(R.id.tvMemberCount);
        layoutAttachment = findViewById(R.id.layoutAttachment);
        layoutPreview = findViewById(R.id.layoutPreview);
        ivPreview = findViewById(R.id.ivPreview);
        btnCancelPreview = findViewById(R.id.btnCancelPreview);
        btnVoiceCall = findViewById(R.id.btnVoiceCall);
        btnVideoCall = findViewById(R.id.btnVideoCall);

        // Set group info
        tvGroupName.setText(groupName);
        Glide.with(this)
                .load(groupAvatar)
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(ivGroupAvatar);

        loadMemberCount();

        btnCancelPreview.setOnClickListener(v -> {
            layoutPreview.setVisibility(View.GONE);
            selectedImageUri = null;
            capturedImage = null;
            if (etMessage.getText().toString().trim().isEmpty()) {
                btnSend.setVisibility(View.GONE);
            }
        });
    }

    private void setupToolbar() {
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        findViewById(R.id.layoutGroupInfo).setOnClickListener(v -> showGroupInfo());
    }

    private void setupRecyclerView() {
        chatAdapter = new GroupChatAdapter(messageList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(chatAdapter);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());

        btnAttachment.setOnClickListener(v -> {
            if (layoutAttachment.getVisibility() == View.VISIBLE) {
                layoutAttachment.setVisibility(View.GONE);
            } else {
                layoutAttachment.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.btnGallery).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        });

        findViewById(R.id.btnCamera).setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        });

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

    private void showGroupInfo() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_group_info, null);

        // Setup group info views
        ImageView ivGroupAvatar = bottomSheetView.findViewById(R.id.ivGroupAvatar);
        TextView tvGroupName = bottomSheetView.findViewById(R.id.tvGroupName);
        RecyclerView rvMembers = bottomSheetView.findViewById(R.id.rvMembers);
        com.google.android.material.button.MaterialButton btnLeaveGroup = bottomSheetView.findViewById(R.id.btnLeaveGroup);

        Glide.with(this)
                .load(groupAvatar)
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(ivGroupAvatar);
        tvGroupName.setText(groupName);

        // Load and display members
        apiService.getGroupMembers(groupId).enqueue(new Callback<List<GroupMemberResponse>>() {
            @Override
            public void onResponse(Call<List<GroupMemberResponse>> call, Response<List<GroupMemberResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GroupMemberResponse> members = response.body();

                    // Kiểm tra xem user hiện tại có phải admin không
                    boolean isAdmin = members.stream().anyMatch(member -> member.getUserId().equals(currentUserId) && "admin".equals(member.getRole()));

                    // Thay đổi text và hành động nút dựa vào vai trò
                    if (isAdmin) {
                        btnLeaveGroup.setText("Giải tán nhóm");
                        btnLeaveGroup.setBackgroundTintList(getColorStateList(R.color.red));
                    } else {
                        btnLeaveGroup.setText("Rời nhóm");
                    }

                    // Thiết lập sự kiện cho nút
                    btnLeaveGroup.setOnClickListener(v -> {
                        String message = isAdmin ? "Bạn có chắc muốn giải tán nhóm này?" : "Bạn có chắc muốn rời khỏi nhóm này?";
                        String title = isAdmin ? "Giải tán nhóm" : "Rời nhóm";
                        String buttonText = isAdmin ? "Giải tán" : "Rời nhóm";

                        // Hiển thị dialog xác nhận
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(GroupChatActivity.this);
                        builder.setTitle(title);
                        builder.setMessage(message);
                        builder.setPositiveButton(buttonText, (dialog, which) -> {
                            if (isAdmin) {
                                deleteGroup();
                            } else {
                                leaveGroup();
                            }
                            bottomSheet.dismiss();
                        });
                        builder.setNegativeButton("Hủy", (dialog, which) -> {
                            dialog.dismiss();
                        });
                        builder.show();
                    });

                    // Cập nhật số lượng thành viên
                    tvMemberCount.setText(members.size() + " thành viên");

                    // Thiết lập RecyclerView cho danh sách thành viên
                    rvMembers.setLayoutManager(new LinearLayoutManager(GroupChatActivity.this));
                    GroupMemberAdapter memberAdapter = new GroupMemberAdapter(members);
                    rvMembers.setAdapter(memberAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<GroupMemberResponse>> call, Throwable t) {
                Log.e("GroupChat", "Error loading members", t);
                Toast.makeText(GroupChatActivity.this,
                        "Không thể tải danh sách thành viên. Vui lòng kiểm tra kết nối mạng",
                        Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheet.setContentView(bottomSheetView);
        bottomSheet.show();
    }

    private void deleteGroup() {
        apiService.removeGroupMember(groupId, currentUserId, true).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GroupChatActivity.this,
                            "Nhóm đã được xóa",
                            Toast.LENGTH_SHORT).show();
                    finish(); // Quay về màn hình trước
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e("GroupChat", "Server error: " + errorBody);
                    } catch (Exception e) {
                        Log.e("GroupChat", "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(GroupChatActivity.this,
                        "Không thể xóa nhóm. Kiểm tra kết nối mạng",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Thêm phương thức xử lý rời nhóm
    private void leaveGroup() {
        apiService.removeGroupMember(groupId, currentUserId, false).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GroupChatActivity.this,
                            "Đã rời khỏi nhóm", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng activity và quay lại màn hình trước
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e("GroupChat", "Server error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("GroupChat", "Error reading error response", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("GroupChat", "Error leaving group", t);
                Toast.makeText(GroupChatActivity.this,
                        "Không thể rời khỏi nhóm. Vui lòng kiểm tra kết nối mạng",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadMemberCount() {
        apiService.getGroupMembers(groupId).enqueue(new Callback<List<GroupMemberResponse>>() {
            @Override
            public void onResponse(Call<List<GroupMemberResponse>> call, Response<List<GroupMemberResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GroupMemberResponse> members = response.body();
                    int memberCount = response.body().size();
                    List<ZegoUIKitUser> invitees = members.stream()
                            .filter(member -> !member.getName().equals(prefManager.getUser().getUsername()))
                            .map(member -> new ZegoUIKitUser(member.getName()))
                            .collect(Collectors.toList());

                    btnVoiceCall.setIsVideoCall(false);
                    btnVoiceCall.setResourceID("zego_uikit_call");
                    btnVoiceCall.setInvitees(invitees);

                    btnVideoCall.setIsVideoCall(true);
                    btnVideoCall.setResourceID("zego_uikit_call");
                    btnVideoCall.setInvitees(invitees);
                    tvMemberCount.setText(memberCount + " thành viên");
                }
            }

            @Override
            public void onFailure(Call<List<GroupMemberResponse>> call, Throwable t) {
                Log.e("GroupChat", "Error loading member count", t);
                Toast.makeText(GroupChatActivity.this,
                        "Không thể tải thông tin thành viên",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        apiService.getGroupMessages(groupId).enqueue(new Callback<List<GroupMessageResponse>>() {
            @Override
            public void onResponse(Call<List<GroupMessageResponse>> call, Response<List<GroupMessageResponse>> response) {


                if (response.isSuccessful() && response.body() != null) {
                    messageList.clear();
                    messageList.addAll(response.body());
                    chatAdapter.notifyDataSetChanged();
                    if (!messageList.isEmpty()) {
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    }
                } else {
                    Log.e("GroupChat", "Error loading messages: " + response.code());
                    Toast.makeText(GroupChatActivity.this,
                            "Không thể tải tin nhắn. Mã lỗi: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GroupMessageResponse>> call, Throwable t) {
                // Ẩn loading nếu có
                // progressBar.setVisibility(View.GONE);

                Log.e("GroupChat", "Error loading messages", t);
                Toast.makeText(GroupChatActivity.this,
                        "Không thể tải tin nhắn. Vui lòng kiểm tra kết nối mạng",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        if (layoutPreview.getVisibility() == View.VISIBLE && (selectedImageUri != null || capturedImage != null)) {
            if (selectedImageUri != null) {
                CloudinaryUtils.uploadImage(this, selectedImageUri, new CloudinaryUtils.UploadCallback() {
                    @Override
                    public void onSuccess(String url) {
                        sendGroupImageMessage(url);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(GroupChatActivity.this,
                                "Lỗi tải ảnh: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (capturedImage != null) {
                CloudinaryUtils.uploadImage(this, capturedImage, new CloudinaryUtils.UploadCallback() {
                    @Override
                    public void onSuccess(String url) {
                        sendGroupImageMessage(url);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(GroupChatActivity.this,
                                "Lỗi tải ảnh: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            String messageText = etMessage.getText().toString().trim();
            if (messageText.isEmpty()) return;

            MessageRequest message = new MessageRequest();
            message.setSenderId(currentUserId);
            message.setReceiverId(groupId);
            message.setContent(messageText);
            message.setStatus("SENT");
            message.setGroup(true);
            message.setTimestamp(LocalDateTime.now().toString());

            sendGroupTextMessage(message);
        }
    }

    private void sendGroupImageMessage(String imageUrl) {
        MessageRequest message = new MessageRequest();
        message.setSenderId(currentUserId);
        message.setReceiverId(groupId);
        message.setMediaType("IMAGE");
        message.setMediaUrl(imageUrl);
        message.setStatus("SENT");
        message.setGroup(true);
        message.setTimestamp(LocalDateTime.now().toString());

        apiService.sendGroupMessage(message).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        // Reset trạng thái xem trước
                        layoutPreview.setVisibility(View.GONE);
                        selectedImageUri = null;
                        capturedImage = null;
                        btnSend.setVisibility(View.GONE);

                        // Tạo tin nhắn mới với đầy đủ thông tin
                        GroupMessageResponse newMessage = new GroupMessageResponse();
                        newMessage.setGroupId(groupId);
                        newMessage.setSenderId(currentUserId);
                        newMessage.setMediaType("IMAGE");
                        newMessage.setMediaUrl(imageUrl);
                        newMessage.setTimestamp(message.getTimestamp());
                        // Thêm thông tin người gửi từ SharedPreferences
                        newMessage.setSenderName(prefManager.getUser().getUsername());
                        newMessage.setSenderAvatar(prefManager.getUser().getAvatarURL());
                        newMessage.setStatus("SENT");

                        // Thêm vào danh sách và cập nhật UI
                        messageList.add(newMessage);
                        chatAdapter.notifyItemInserted(messageList.size() - 1);
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    });
                } else {
                    runOnUiThread(() -> {
                        Log.e("GroupChat", "Error sending image: " + response.code());
                        Toast.makeText(GroupChatActivity.this,
                                "Không thể gửi ảnh. Mã lỗi: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                runOnUiThread(() -> {
                    Log.e("GroupChat", "Error sending image", t);
                    Toast.makeText(GroupChatActivity.this,
                            "Không thể gửi ảnh. Vui lòng kiểm tra kết nối mạng",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void sendGroupTextMessage(MessageRequest message) {
        apiService.sendGroupMessage(message).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    etMessage.setText("");
                    GroupMessageResponse newMessage = new GroupMessageResponse();
                    newMessage.setSenderId(currentUserId);
                    newMessage.setGroupId(groupId);
                    newMessage.setContent(message.getContent());
                    newMessage.setTimestamp(message.getTimestamp());
                    // Thêm thông tin người gửi
                    newMessage.setSenderName(prefManager.getUser().getUsername());
                    newMessage.setSenderAvatar(prefManager.getUser().getAvatarURL());
                    messageList.add(newMessage);
                    runOnUiThread(() -> {
                        chatAdapter.notifyItemInserted(messageList.size() - 1);
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(GroupChatActivity.this,
                                "Lỗi gửi tin nhắn: " + response.body(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupChatActivity.this,
                            "Lỗi kết nối: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
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
                        capturedImage = (Bitmap) data.getExtras().get("data");
                        ivPreview.setImageBitmap(capturedImage);
                        layoutPreview.setVisibility(View.VISIBLE);
                        layoutAttachment.setVisibility(View.GONE);
                        btnSend.setVisibility(View.VISIBLE);
                    }
                    break;
                case REQUEST_PICK_IMAGE:
                    if (data != null && data.getData() != null) {
                        selectedImageUri = data.getData();
                        Glide.with(this)
                                .load(selectedImageUri)
                                .into(ivPreview);
                        layoutPreview.setVisibility(View.VISIBLE);
                        layoutAttachment.setVisibility(View.GONE);
                        btnSend.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    }

    @Override
    public void onMessageReceived(String message) {
        try {
            JSONObject jsonMessage = new JSONObject(message);
            String type = jsonMessage.getString("type");
            if ("GROUP_MESSAGE".equals(type)) {
                Long messageGroupId = jsonMessage.getLong("groupId");
                if (messageGroupId.equals(groupId)) {
                    runOnUiThread(this::loadMessages);
                }
            }
        } catch (JSONException e) {
            Log.e("GroupChat", "Error parsing message", e);
        }
    }

    @Override
    public void onNotificationReceived(NotificationResponse notification) {
        if ("GROUP_MESSAGE".equals(notification.getType())) {
            runOnUiThread(this::loadMessages);
        }
    }

    @Override
    public void onTypingStatusReceived(String typing) {
        // TODO: Implement typing status for group chat
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void startPolling() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadMessages();
                handler.postDelayed(this, POLLING_INTERVAL);
            }
        }, POLLING_INTERVAL);
    }
}