package ute.nhom27.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import ute.nhom27.android.R;
import ute.nhom27.android.model.response.MessageListResponse;
import ute.nhom27.android.view.activities.ChatActivity;
import ute.nhom27.android.view.activities.GroupChatActivity;
import ute.nhom27.android.utils.DateTimeUtils;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private List<MessageListResponse> messageList;
    private Context context;

    public MessageListAdapter(List<MessageListResponse> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageListResponse message = messageList.get(position);

        holder.tvName.setText(message.getName());
        holder.tvLastMessage.setText(message.getLastMessage());

        // Hiển thị thời gian
        if (message.getLastMessageTime() != null) {
            holder.tvTime.setText(DateTimeUtils.getRelativeTimeSpan(message.getLastMessageTime()));
            holder.tvTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }

        // Hiển thị số tin nhắn chưa đọc
        if (message.getUnreadCount() > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(message.getUnreadCount()));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        // Hiển thị avatar
        if (message.getAvatar() != null && !message.getAvatar().isEmpty()) {
            Glide.with(context)
                    .load(message.getAvatar())
                    .placeholder(message.isGroup() ? R.drawable.default_avatar : R.drawable.default_avatar)
                    .error(message.isGroup() ? R.drawable.default_avatar : R.drawable.default_avatar)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(message.isGroup() ? R.drawable.default_avatar : R.drawable.default_avatar);
        }

        // Xử lý click
        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if (message.isGroup()) {
                // Mở GroupChatActivity cho tin nhắn nhóm
                intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId", message.getId());
                intent.putExtra("groupName", message.getName());
                intent.putExtra("groupAvatar", message.getAvatar());
            } else {
                // Mở ChatActivity cho tin nhắn cá nhân
                intent = new Intent(context, ChatActivity.class);
                intent.putExtra("receiverId", message.getId());
                intent.putExtra("receiverName", message.getName());
                intent.putExtra("receiverAvatar", message.getAvatar());
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        TextView tvLastMessage;
        TextView tvTime;
        TextView tvUnreadCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
        }
    }
}