package ute.nhom27.android.adapter;

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
import ute.nhom27.android.model.response.UserResponse;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.UserViewHolder> {

    private List<UserResponse> userList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(UserResponse user);
    }

    public FriendListAdapter(List<UserResponse> userList, OnItemClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    public void updateData(List<UserResponse> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(userList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        TextView nameText;
        View acceptButton, rejectButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.user_avatar);
            nameText = itemView.findViewById(R.id.user_name);
            acceptButton = itemView.findViewById(R.id.btn_accept);
            rejectButton = itemView.findViewById(R.id.btn_reject);
        }

        public void bind(UserResponse user, OnItemClickListener listener) {
            nameText.setText(user.getUsername());

            String avatarUrl = user.getAvatarURL();
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.default_avatar)
                        .circleCrop()
                        .into(avatarImage);
            } else {
                avatarImage.setImageResource(R.drawable.default_avatar);
            }

            if (acceptButton != null) acceptButton.setVisibility(View.GONE);
            if (rejectButton != null) rejectButton.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> listener.onItemClick(user));
        }
    }

}