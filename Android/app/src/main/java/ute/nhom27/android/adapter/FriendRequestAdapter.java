package ute.nhom27.android.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.R;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.model.response.UserResponse;
import ute.nhom27.android.network.WebSocketClient;
import ute.nhom27.android.utils.SharedPrefManager;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private List<UserResponse> requestList;
    private Context context;
    private OnItemActionListener listener;
    private WebSocketClient webSocketClient;

    public interface OnItemActionListener {
        void onItemClick(UserResponse user);
    }

    public FriendRequestAdapter(List<UserResponse> requestList, Context context, WebSocketClient webSocketClient, OnItemActionListener listener) {
        this.requestList = requestList;
        this.context = context;
        this.webSocketClient = webSocketClient;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        UserResponse user = requestList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        TextView nameText;
        View acceptButton, rejectButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.user_avatar);
            nameText = itemView.findViewById(R.id.user_name);
            acceptButton = itemView.findViewById(R.id.btn_accept);
            rejectButton = itemView.findViewById(R.id.btn_reject);
        }

        public void bind(UserResponse user, OnItemActionListener listener) {
            nameText.setText(user.getUsername());
            if (user.getAvatarURL() != null && !user.getAvatarURL().trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(user.getAvatarURL())
                        .placeholder(R.drawable.default_avatar)
                        .circleCrop()
                        .into(avatarImage);
            } else {
                avatarImage.setImageResource(R.drawable.default_avatar);
            }
            itemView.setOnClickListener(v -> listener.onItemClick(user));

            acceptButton.setOnClickListener(v -> {
                SharedPrefManager sharedPrefManager = new SharedPrefManager(v.getContext());
                Long currentUserId = sharedPrefManager.getUser().getId();
                Long senderId = user.getId();

                ApiService apiService = ApiClient.getAuthClient(v.getContext()).create(ApiService.class);
                apiService.acceptFriendRequest(currentUserId, senderId).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            // Xóa khỏi danh sách và cập nhật RecyclerView
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                requestList.remove(position);
                                notifyItemRemoved(position);
                            }
                            // Gửi thông báo WebSocket
                            sendWebSocketNotification(currentUserId, senderId, "FRIEND_ACCEPT");
                        } else {
                            Toast.makeText(itemView.getContext(), "Lỗi khi chấp nhận", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(itemView.getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            rejectButton.setOnClickListener(v -> {
                SharedPrefManager sharedPrefManager = new SharedPrefManager(v.getContext());
                Long currentUserId = sharedPrefManager.getUser().getId();
                Long senderId = user.getId();

                ApiService apiService = ApiClient.getAuthClient(v.getContext()).create(ApiService.class);
                apiService.rejectFriendRequest(currentUserId, senderId).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            // Xóa khỏi danh sách và cập nhật RecyclerView
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                requestList.remove(position);
                                notifyItemRemoved(position);
                            }
                            // Gửi thông báo WebSocket
                            sendWebSocketNotification(currentUserId, senderId, "FRIEND_REJECT");
                        } else {
                            Toast.makeText(itemView.getContext(), "Lỗi khi từ chối", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(itemView.getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        private void sendWebSocketNotification(Long receiverId, Long senderId, String type) {
            if (webSocketClient != null) {
                try {
                    JSONObject notification = new JSONObject();
                    notification.put("senderId", receiverId);
                    notification.put("receiverId", senderId);
                    notification.put("type", type);
                    notification.put("usernameSender", new SharedPrefManager(context).getUser().getUsername());
                    notification.put("content", type.equals("FRIEND_ACCEPT") ?
                            " đã chấp nhận lời mời kết bạn của bạn" :
                            " đã từ chối lời mời kết bạn của bạn");
                    // Gửi đến /app/notification.send thay vì /app/chat.sendMessage
                    webSocketClient.sendMessage(notification.toString());
                } catch (JSONException e) {
                    Toast.makeText(context, "Lỗi gửi thông báo WebSocket", Toast.LENGTH_SHORT).show();
                    Log.e("WebSocket", "Error creating JSON: " + e.getMessage());
                }
            } else {
                Toast.makeText(context, "WebSocket không được khởi tạo", Toast.LENGTH_SHORT).show();
            }
        }
    }
}