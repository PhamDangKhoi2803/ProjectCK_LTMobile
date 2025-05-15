package ute.nhom27.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import ute.nhom27.android.R;
import ute.nhom27.android.model.response.MessageResponse;

public class AIChatAdapter extends RecyclerView.Adapter<AIChatAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private Context context;
    private List<MessageResponse> messageList;
    private Long currentUserId;

    public AIChatAdapter(List<MessageResponse> messageList, Long currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view;

        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
        }

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageResponse message = messageList.get(position);

        // Set message content
        holder.messageText.setText(message.getContent());

        // Format and display timestamp
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

        // Show sender name for AI messages
        if (getItemViewType(position) == VIEW_TYPE_AI) {
            holder.tvSenderName.setText("AI Assistant");
            holder.tvSenderName.setVisibility(View.VISIBLE);
        } else {
            holder.tvSenderName.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageResponse message = messageList.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_AI;
        }
    }
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView tvTime;
        TextView tvSenderName;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
        }
    }
}