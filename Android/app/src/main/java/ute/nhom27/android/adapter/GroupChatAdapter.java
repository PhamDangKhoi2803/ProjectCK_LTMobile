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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ute.nhom27.android.R;
import ute.nhom27.android.model.response.GroupMessageResponse;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<GroupMessageResponse> messageList;
    private Long currentUserId;

    public GroupChatAdapter(List<GroupMessageResponse> messageList, Long currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view;

        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
        }

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        GroupMessageResponse message = messageList.get(position);

        // Hiển thị tên người gửi cho tin nhắn nhận
        if (getItemViewType(position) == VIEW_TYPE_RECEIVED) {
            holder.tvSenderName.setText(message.getSenderName());
            holder.tvSenderName.setVisibility(View.VISIBLE);

            // Load avatar người gửi
            Glide.with(context)
                    .load(message.getSenderAvatar())
                    .placeholder(R.drawable.default_avatar)
                    .circleCrop()
                    .into(holder.ivSenderAvatar);
            holder.ivSenderAvatar.setVisibility(View.VISIBLE);
        }

        if ("IMAGE".equals(message.getMediaType())) {
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.GONE);

            Glide.with(context)
                    .load(message.getMediaUrl())
                    .placeholder(R.drawable.default_avatar)
                    .into(holder.messageImage);
        } else {
            holder.messageImage.setVisibility(View.GONE);
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setText(message.getContent());
        }

        // Format và hiển thị thời gian
        if (message.getTimestamp() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(message.getTimestamp());
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String time = outputFormat.format(date);
                holder.tvTime.setText(time);
                holder.tvTime.setVisibility(View.VISIBLE);
            } catch (ParseException e) {
                e.printStackTrace();
                holder.tvTime.setVisibility(View.GONE);
            }
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        GroupMessageResponse message = messageList.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView messageImage;
        TextView tvTime;
        TextView tvSenderName;
        ImageView ivSenderAvatar;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tvMessage);
            messageImage = itemView.findViewById(R.id.ivImage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            ivSenderAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}