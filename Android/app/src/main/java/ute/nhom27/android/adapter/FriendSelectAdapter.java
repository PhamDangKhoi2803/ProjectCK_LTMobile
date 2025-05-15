package ute.nhom27.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import ute.nhom27.android.R;
import ute.nhom27.android.model.response.UserResponse;

public class FriendSelectAdapter extends RecyclerView.Adapter<FriendSelectAdapter.ViewHolder> {
    private List<UserResponse> friends;
    private Set<Long> selectedFriends;

    public FriendSelectAdapter() {
        this.friends = new ArrayList<>();
        this.selectedFriends = new HashSet<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserResponse friend = friends.get(position);
        holder.tvUsername.setText(friend.getUsername());

        if (friend.getAvatarURL() != null && !friend.getAvatarURL().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(friend.getAvatarURL())
                    .placeholder(R.drawable.default_avatar)
                    .into(holder.ivAvatar);
        }

        boolean isSelected = selectedFriends.contains(friend.getId());
        updateButtonState(holder.btnAction, isSelected);

        holder.btnAction.setOnClickListener(v -> {
            if (isSelected) {
                selectedFriends.remove(friend.getId());
            } else {
                selectedFriends.add(friend.getId());
            }
            updateButtonState(holder.btnAction, !isSelected);
        });
    }

    private void updateButtonState(MaterialButton button, boolean isSelected) {
        if (isSelected) {
            button.setText("Hủy");
        } else {
            button.setText("Thêm");
        }
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void setFriends(List<UserResponse> friends) {
        this.friends = friends;
        notifyDataSetChanged();
    }

    public Set<Long> getSelectedFriends() {
        return selectedFriends;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvUsername;
        MaterialButton btnAction;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
} 