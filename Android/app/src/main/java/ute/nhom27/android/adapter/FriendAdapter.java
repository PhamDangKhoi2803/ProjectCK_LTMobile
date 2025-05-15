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
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import ute.nhom27.android.R;
import ute.nhom27.android.model.response.UserResponse;
import ute.nhom27.android.network.WebSocketClient;
import ute.nhom27.android.utils.SharedPrefManager;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    public static final int TYPE_FRIEND_LIST = 0;
    public static final int TYPE_FRIEND_REQUEST = 1;
    public static final int TYPE_SENT_REQUEST = 2;
    public static final int TYPE_SUGGESTION = 3;

    private List<UserResponse> userList;
    private Context context;
    private int adapterType;
    private WebSocketClient webSocketClient;

    private OnWithdrawClickListener withdrawClickListener;
    private OnAddFriendClickListener addFriendClickListener;
    private OnAcceptClickListener acceptClickListener;
    private OnRejectClickListener rejectClickListener;
    private OnItemClickListener itemClickListener;
    private OnMessageClickListener messageClickListener;
    private OnUnfriendClickListener unfriendClickListener;

    public interface OnMessageClickListener {
        void onMessageClick(UserResponse user);
    }

    public interface OnUnfriendClickListener {
        void onUnfriendClick(UserResponse user, int position);
    }

    public interface OnWithdrawClickListener {
        void onWithdrawClick(UserResponse user, int position);
    }

    public interface OnAddFriendClickListener {
        void onAddFriendClick(UserResponse user, int position);
    }

    public interface OnAcceptClickListener {
        void onAcceptClick(UserResponse user, int position);
    }

    public interface OnRejectClickListener {
        void onRejectClick(UserResponse user, int position);
    }

    public interface OnItemClickListener {
        void onItemClick(UserResponse user);
    }

    public FriendAdapter(List<UserResponse> userList, int adapterType) {
        this.userList = userList;
        this.adapterType = adapterType;
    }

    public void setOnWithdrawClickListener(OnWithdrawClickListener listener) {
        this.withdrawClickListener = listener;
    }

    public void setOnAddFriendClickListener(OnAddFriendClickListener listener) {
        this.addFriendClickListener = listener;
    }

    public void setOnAcceptClickListener(OnAcceptClickListener listener) {
        this.acceptClickListener = listener;
    }

    public void setOnRejectClickListener(OnRejectClickListener listener) {
        this.rejectClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.messageClickListener = listener;
    }

    public void setOnUnfriendClickListener(OnUnfriendClickListener listener) {
        this.unfriendClickListener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        UserResponse user = userList.get(position);
        holder.bind(user, position);
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public void updateData(List<UserResponse> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < userList.size()) {
            userList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void addItem(UserResponse user) {
        userList.add(0, user);
        notifyItemInserted(0);
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        TextView nameText;
        MaterialButton btnAccept, btnReject, btnWithdraw, btnAddFriend, btnMessage, btnUnfriend;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.user_avatar);
            nameText = itemView.findViewById(R.id.user_name);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnWithdraw = itemView.findViewById(R.id.btn_withdraw);
            btnAddFriend = itemView.findViewById(R.id.btn_add_friend);
            btnMessage = itemView.findViewById(R.id.btn_message);
            btnUnfriend = itemView.findViewById(R.id.btn_unfriend);
        }

        public void bind(UserResponse user, int position) {
            nameText.setText(user.getUsername());

            // Load avatar
            if (user.getAvatarURL() != null && !user.getAvatarURL().trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(user.getAvatarURL())
                        .placeholder(R.drawable.default_avatar)
                        .circleCrop()
                        .into(avatarImage);
            } else {
                avatarImage.setImageResource(R.drawable.default_avatar);
            }

            // Hide all buttons by default
            btnAccept.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
            btnWithdraw.setVisibility(View.GONE);
            btnAddFriend.setVisibility(View.GONE);
            btnMessage.setVisibility(View.GONE);
            btnUnfriend.setVisibility(View.GONE);

            // Show appropriate buttons based on adapter type
            switch (adapterType) {
                case TYPE_FRIEND_LIST:
                    btnMessage.setVisibility(View.VISIBLE);
                    btnUnfriend.setVisibility(View.VISIBLE);
                    
                    btnMessage.setOnClickListener(v -> {
                        if (messageClickListener != null) {
                            messageClickListener.onMessageClick(user);
                        }
                    });
                    
                    btnUnfriend.setOnClickListener(v -> {
                        if (unfriendClickListener != null) {
                            unfriendClickListener.onUnfriendClick(user, position);
                        }
                    });
                    break;

                case TYPE_FRIEND_REQUEST:
                    btnAccept.setVisibility(View.VISIBLE);
                    btnReject.setVisibility(View.VISIBLE);
                    btnAccept.setOnClickListener(v -> {
                        if (acceptClickListener != null) {
                            acceptClickListener.onAcceptClick(user, position);
                        }
                    });
                    btnReject.setOnClickListener(v -> {
                        if (rejectClickListener != null) {
                            rejectClickListener.onRejectClick(user, position);
                        }
                    });
                    break;

                case TYPE_SENT_REQUEST:
                    btnWithdraw.setVisibility(View.VISIBLE);
                    btnWithdraw.setOnClickListener(v -> {
                        if (withdrawClickListener != null) {
                            withdrawClickListener.onWithdrawClick(user, position);
                        }
                    });
                    break;

                case TYPE_SUGGESTION:
                    btnAddFriend.setVisibility(View.VISIBLE);
                    btnAddFriend.setOnClickListener(v -> {
                        if (addFriendClickListener != null) {
                            addFriendClickListener.onAddFriendClick(user, position);
                        }
                    });
                    break;
            }

            // Set item click listener
            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(user);
                }
            });
        }
    }

    public void sendWebSocketNotification(Long senderId, Long receiverId, String type) {
        if (webSocketClient != null) {
            try {
                JSONObject notification = new JSONObject();
                notification.put("senderId", senderId);
                notification.put("receiverId", receiverId);
                notification.put("type", type);
                notification.put("usernameSender", new SharedPrefManager(context).getUser().getUsername());
                
                String content = "";
                switch (type) {
                    case "FRIEND_REQUEST":
                        content = " đã gửi lời mời kết bạn";
                        break;
                    case "FRIEND_REQUEST_WITHDRAWN":
                        content = " đã thu hồi lời mời kết bạn";
                        break;
                    case "FRIEND_ACCEPT":
                        content = " đã chấp nhận lời mời kết bạn của bạn";
                        break;
                    case "FRIEND_REJECT":
                        content = " đã từ chối lời mời kết bạn của bạn";
                        break;
                    case "UNFRIEND":
                        content = " đã hủy kết bạn với bạn";
                        break;
                }
                notification.put("content", content);
                
                webSocketClient.sendMessage(notification.toString());
            } catch (JSONException e) {
                Log.e("WebSocket", "Error creating JSON: " + e.getMessage());
                Toast.makeText(context, "Lỗi gửi thông báo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }
} 