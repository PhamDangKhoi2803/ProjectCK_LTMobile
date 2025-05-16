package ute.nhom27.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ute.nhom27.android.R;
import ute.nhom27.android.api.ApiClient;
import ute.nhom27.android.api.ApiService;
import ute.nhom27.android.model.response.UserResponse;
import ute.nhom27.android.network.WebSocketClient;
import ute.nhom27.android.utils.SharedPrefManager;

public class FriendSuggestionAdapter extends RecyclerView.Adapter<FriendSuggestionAdapter.ViewHolder> implements Filterable {

    private List<UserResponse> userList;
    private List<UserResponse> userListFull;
    private final Context context;
    private WebSocketClient webSocketClient;
    private ApiService apiService;

    public FriendSuggestionAdapter(List<UserResponse> userList, Context context) {
        this.userList = userList;
        this.userListFull = new ArrayList<>(userList);
        this.context = context;
        this.apiService = ApiClient.getAuthClient(context).create(ApiService.class);
    }

    public void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserResponse user = userList.get(position);
        SharedPrefManager sharedPrefManager = new SharedPrefManager(context);
        Long currentUserId = sharedPrefManager.getUser().getId();

        holder.userName.setText(user.getUsername());

        // Load avatar
        if (user.getAvatarURL() != null && !user.getAvatarURL().isEmpty()) {
            Glide.with(context)
                    .load(user.getAvatarURL())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(holder.userAvatar);
        } else {
            holder.userAvatar.setImageResource(R.drawable.default_avatar);
        }

        // Reset visibility of all buttons
        holder.btnAddFriend.setVisibility(View.VISIBLE);
        holder.btnWithdraw.setVisibility(View.GONE);
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnReject.setVisibility(View.GONE);
        holder.btnMessage.setVisibility(View.GONE);
        holder.btnUnfriend.setVisibility(View.GONE);

        // Add friend button click
        holder.btnAddFriend.setOnClickListener(v -> {
            apiService.sendFriendRequest(currentUserId, user.getId())
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(context, "Đã gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();

                                // Send notification via WebSocket if available
                                if (webSocketClient != null) {
                                    try {

                                        // Reload the list through websocket
                                        apiService.getNonFriendUsers(currentUserId).enqueue(new Callback<List<UserResponse>>() {
                                            @Override
                                            public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    updateData(response.body());
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                                                // Ignore any errors
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Lỗi khi gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public Filter getFilter() {
        return userFilter;
    }

    private Filter userFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<UserResponse> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(userListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (UserResponse user : userListFull) {
                    if (user.getUsername().toLowerCase().contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            userList.clear();
            userList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public void updateData(List<UserResponse> newUsers) {
        userList.clear();
        userList.addAll(newUsers);
        userListFull.clear();
        userListFull.addAll(newUsers);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userAvatar;
        TextView userName;
        MaterialButton btnAddFriend;
        MaterialButton btnWithdraw;
        MaterialButton btnAccept;
        MaterialButton btnReject;
        MaterialButton btnMessage;
        MaterialButton btnUnfriend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.user_avatar);
            userName = itemView.findViewById(R.id.user_name);
            btnAddFriend = itemView.findViewById(R.id.btn_add_friend);
            btnWithdraw = itemView.findViewById(R.id.btn_withdraw);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnMessage = itemView.findViewById(R.id.btn_message);
            btnUnfriend = itemView.findViewById(R.id.btn_unfriend);
        }
    }
}