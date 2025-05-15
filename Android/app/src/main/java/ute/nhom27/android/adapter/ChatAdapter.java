package ute.nhom27.android.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ute.nhom27.android.R;
import ute.nhom27.android.model.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<ChatMessage> messages;
    private Long currentUserId;

    public ChatAdapter(List<ChatMessage> messages, Long currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        if (message.getSender().getId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof SentMessageHolder) {
            SentMessageHolder sentHolder = (SentMessageHolder) holder;
            sentHolder.tvMessage.setText(message.getContent());
            sentHolder.tvTime.setText(formatTime(message.getTimestamp()));
            sentHolder.tvStatus.setText(message.getStatus());

            // Set status icon
            if ("SEEN".equals(message.getStatus())) {
                sentHolder.ivStatus.setImageResource(R.drawable.ic_delivered);
            } else {
                sentHolder.ivStatus.setImageResource(R.drawable.ic_sent);
            }
        } else if (holder instanceof ReceivedMessageHolder) {
            ReceivedMessageHolder receivedHolder = (ReceivedMessageHolder) holder;
            receivedHolder.tvMessage.setText(message.getContent());
            receivedHolder.tvTime.setText(formatTime(message.getTimestamp()));
            receivedHolder.tvSenderName.setText(message.getSender().getUsername());

            // Load avatar using Glide
            Glide.with(receivedHolder.ivAvatar.getContext())
                    .load(message.getSender().getAvatarURL())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(receivedHolder.ivAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String formatTime(String timestamp) {
//        if (timestamp == null) {
//            return "";
//        }
//        try {
//            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault());
//            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
//            Date date = inputFormat.parse(timestamp);
//            return outputFormat.format(date);
//        } catch (ParseException e) {
//            return timestamp;
//        }

//        if (timestamp == null || timestamp.isEmpty()) {
//            return "";
//        }

        try {
            // Định dạng timestamp từ LocalDateTime
            DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, inputFormatter);

            // Format thời gian hiển thị
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return dateTime.format(outputFormatter);

        } catch (Exception e) {
            Log.e("ChatAdapter", "Error parsing timestamp: " + timestamp, e);
            return "";
        }
    }

    // ViewHolder classes
    static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvStatus;
        ImageView ivStatus;

        SentMessageHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivStatus = itemView.findViewById(R.id.ivStatus);
        }
    }

    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvSenderName;
        ImageView ivAvatar;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}
