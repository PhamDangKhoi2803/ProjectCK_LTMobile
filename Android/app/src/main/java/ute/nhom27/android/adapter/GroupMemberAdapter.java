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
import ute.nhom27.android.model.response.GroupMemberResponse;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder> {
    private List<GroupMemberResponse> members;
    private Context context;

    public GroupMemberAdapter(List<GroupMemberResponse> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        GroupMemberResponse member = members.get(position);

        holder.tvUsername.setText(member.getName());
        holder.tvRole.setText(member.getRole());

        // Load avatar với Glide
        Glide.with(context)
                .load(member.getAvatar())
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(holder.ivAvatar);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername;
        TextView tvRole;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUsername = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
        }
    }
}