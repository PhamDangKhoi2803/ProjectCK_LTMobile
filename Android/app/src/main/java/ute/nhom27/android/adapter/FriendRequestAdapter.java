package ute.nhom27.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import ute.nhom27.android.R;
import ute.nhom27.android.model.response.UserResponse;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private List<UserResponse> requestList;
    private Context context;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onItemClick(UserResponse user);
        // Bạn có thể thêm các hành động khác, ví dụ: onAccept(UserResponse user), onReject(UserResponse user)
    }

    public FriendRequestAdapter(List<UserResponse> requestList, Context context, OnItemActionListener listener) {
        this.requestList = requestList;
        this.context = context;
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

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        TextView nameText;
        View acceptButton, rejectButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.user_avatar);
            nameText = itemView.findViewById(R.id.user_name);
            // Giả sử bạn mở rộng layout item_user.xml để thêm 2 nút chấp nhận và từ chối
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

            // Xử lý nút chấp nhận và từ chối nếu layout có:
            if (acceptButton != null && rejectButton != null) {
                acceptButton.setOnClickListener(v -> {
                    // TODO: Thực hiện gọi API chấp nhận lời mời cho user này
                    listener.onItemClick(user);
                });
                acceptButton.setOnClickListener(v -> {
                    // TODO: Thực hiện gọi API từ chối lời mời cho user này
                    listener.onItemClick(user);
                });
            }
        }
    }
}
