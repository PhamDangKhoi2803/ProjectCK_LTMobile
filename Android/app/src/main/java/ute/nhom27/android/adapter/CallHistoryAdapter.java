package ute.nhom27.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ute.nhom27.android.R;
import ute.nhom27.android.model.response.CallHistoryResponse;
import ute.nhom27.android.model.response.UserResponse;
import ute.nhom27.android.utils.SharedPrefManager;

public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.CallViewHolder> {
    private List<CallHistoryResponse> callHistoryList;
    private Context context;
    private OnCallHistoryItemClickListener listener;
    private Long currentUserId;
    private Map<Long, UserResponse> userInfoMap; // Map để lưu thông tin người dùng

    public interface OnCallHistoryItemClickListener {
        void onCallClick(CallHistoryResponse callHistory);
    }

    public CallHistoryAdapter(Context context, List<CallHistoryResponse> callHistoryList, OnCallHistoryItemClickListener listener) {
        this.context = context;
        this.callHistoryList = callHistoryList;
        this.listener = listener;
        this.currentUserId = new SharedPrefManager(context).getUser().getId();
        this.userInfoMap = new HashMap<>();
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_call_history, parent, false);
        return new CallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {
        CallHistoryResponse call = callHistoryList.get(position);

        // Xác định người dùng đối diện (người gọi hoặc người nhận)
        boolean isOutgoing = call.getCallerId().equals(currentUserId);
        Long contactId = isOutgoing ? call.getReceiverId() : call.getCallerId();

        // Lấy thông tin người dùng từ map
        UserResponse contactInfo = userInfoMap.get(contactId);
        String contactName = contactInfo != null ? contactInfo.getUsername() : "Unknown";
        String contactAvatar = contactInfo != null ? contactInfo.getAvatarURL() : null;

        // Hiển thị thông tin người dùng
        holder.tvName.setText(contactName);
        Glide.with(context)
                .load(contactAvatar)
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(holder.ivAvatar);

        // Hiển thị loại cuộc gọi (âm thanh/video)
        if ("VIDEO".equals(call.getCallType())) {
            holder.ivCallType.setImageResource(R.drawable.ic_video_call);
        } else {
            holder.ivCallType.setImageResource(R.drawable.ic_voice_call);
        }

        // Hiển thị trạng thái cuộc gọi
        String statusText = "";
        int statusColor = ContextCompat.getColor(context, R.color.black);
        int statusIcon = 0;

        if (isOutgoing) {
            statusText = "Cuộc gọi đi";
            statusIcon = R.drawable.ic_call_made;
        } else {
            statusText = "Cuộc gọi đến";
            statusIcon = R.drawable.ic_call_received;
        }

        if ("MISSED".equals(call.getStatus())) {
            statusText = "Cuộc gọi nhỡ";
            statusColor = ContextCompat.getColor(context, R.color.red);
            statusIcon = R.drawable.ic_call_missed;
        } else if ("DECLINED".equals(call.getStatus())) {
            statusText = "Cuộc gọi bị từ chối";
            statusColor = ContextCompat.getColor(context, R.color.red);
            statusIcon = R.drawable.ic_call_missed;
        }

        holder.tvCallStatus.setText(statusText);
        holder.tvCallStatus.setTextColor(statusColor);
        holder.ivCallStatus.setImageResource(statusIcon);

        // Hiển thị thời gian
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(call.getStartTime());

            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            String formattedTime = outputFormat.format(date);
            holder.tvCallTime.setText(formattedTime);
        } catch (ParseException e) {
            holder.tvCallTime.setText(call.getStartTime());
        }

        // Hiển thị thời lượng cuộc gọi nếu có
        if (call.getDuration() > 0) {
            int minutes = call.getDuration() / 60;
            int seconds = call.getDuration() % 60;
            holder.tvCallDuration.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
            holder.tvCallDuration.setVisibility(View.VISIBLE);
        } else {
            holder.tvCallDuration.setVisibility(View.GONE);
        }

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallClick(call);
            }
        });
    }

    public UserResponse getUserInfo(Long userId) {
        return userInfoMap.get(userId);
    }

    @Override
    public int getItemCount() {
        return callHistoryList != null ? callHistoryList.size() : 0;
    }

    public void updateData(List<CallHistoryResponse> newData) {
        this.callHistoryList = newData;
        notifyDataSetChanged();
    }

    // Thêm thông tin người dùng vào map
    public void addUserInfo(Long userId, UserResponse userInfo) {
        userInfoMap.put(userId, userInfo);
        notifyDataSetChanged();
    }

    // Thêm danh sách thông tin người dùng
    public void addAllUserInfo(Map<Long, UserResponse> userInfoMap) {
        this.userInfoMap.putAll(userInfoMap);
        notifyDataSetChanged();
    }

    // Phương thức để lấy danh sách id người dùng cần lấy thông tin
    public List<Long> getNeededUserIds() {
        List<Long> userIds = new ArrayList<>();
        for (CallHistoryResponse call : callHistoryList) {
            if (call.getCallerId().equals(currentUserId)) {
                if (!userInfoMap.containsKey(call.getReceiverId())) {
                    userIds.add(call.getReceiverId());
                }
            } else {
                if (!userInfoMap.containsKey(call.getCallerId())) {
                    userIds.add(call.getCallerId());
                }
            }
        }
        return userIds;
    }

    static class CallViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivCallType, ivCallStatus;
        TextView tvName, tvCallStatus, tvCallTime, tvCallDuration;

        CallViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivCallType = itemView.findViewById(R.id.ivCallType);
            ivCallStatus = itemView.findViewById(R.id.ivCallStatus);
            tvName = itemView.findViewById(R.id.tvName);
            tvCallStatus = itemView.findViewById(R.id.tvCallStatus);
            tvCallTime = itemView.findViewById(R.id.tvCallTime);
            tvCallDuration = itemView.findViewById(R.id.tvCallDuration);
        }
    }
}