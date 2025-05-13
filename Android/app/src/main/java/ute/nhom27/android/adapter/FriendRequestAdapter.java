package ute.nhom27.android.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.R;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.model.response.UserResponse;
import ute.nhom27.android.utils.SharedPrefManager;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private List<UserResponse> requestList;
    public  Context context;
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

            acceptButton.setOnClickListener(v -> {
                SharedPrefManager sharedPrefManager = new SharedPrefManager(v.getContext());
                Long currentUserId = sharedPrefManager.getUser().getId();
                Long senderId = user.getId();    // ID người gửi lời mời

                ApiService apiService = ApiClient.getAuthClient(v.getContext()).create(ApiService.class);
                apiService.acceptFriendRequest(currentUserId, senderId).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(itemView.getContext(), "Đã chấp nhận", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(itemView.getContext(), "Lỗi khi chấp nhận", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(itemView.getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
            });
            rejectButton.setOnClickListener(v -> {
                SharedPrefManager sharedPrefManager = new SharedPrefManager(v.getContext());
                Long currentUserId = sharedPrefManager.getUser().getId();
                Long senderId = user.getId();

                ApiService apiService = ApiClient.getAuthClient(v.getContext()).create(ApiService.class);
                apiService.rejectFriendRequest(currentUserId, senderId).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(itemView.getContext(), "Đã từ chối", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(itemView.getContext(), "Lỗi khi từ chối", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(itemView.getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }
}
