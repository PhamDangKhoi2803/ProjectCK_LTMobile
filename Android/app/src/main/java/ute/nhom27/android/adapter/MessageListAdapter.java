package ute.nhom27.android.adapter;

import android.content.Context;
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

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private List<MessageListResponse> friendList;
    private Context context;

    public MessageListAdapter(List<MessageListResponse> friendList, Context context) {
        this.friendList = friendList;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView chatName, lastMessage, time, status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            chatName = itemView.findViewById(R.id.chat_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            time = itemView.findViewById(R.id.time);
            status = itemView.findViewById(R.id.message_status);
        }
    }

    @NonNull
    @Override
    public MessageListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageListAdapter.ViewHolder holder, int position) {
        MessageListResponse friend = friendList.get(position);
        holder.chatName.setText(friend.getFriendName());
        holder.lastMessage.setText(friend.getLastMessage());
        holder.time.setText(friend.getTimestamp() != null ? friend.getTimestamp() : "Chưa có thời gian");
        holder.status.setText(Boolean.TRUE.equals(friend.getIsSeen()) ? "Đã xem" : "Đã nhận");

        if (friend.getAvatarUrl() != null) {
            Glide.with(context)
                    .load(friend.getAvatarUrl())
                    .placeholder(R.drawable.default_avatar)
                    .into(holder.avatar);
        } else {
            Glide.with(context)
                    .load(R.drawable.default_avatar)
                    .into(holder.avatar);
        }
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }
}