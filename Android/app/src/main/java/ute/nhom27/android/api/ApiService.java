package ute.nhom27.android.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import ute.nhom27.android.model.AISuggestion;
import ute.nhom27.android.model.ChatGroup;
import ute.nhom27.android.model.ChatMessage;
import ute.nhom27.android.model.Friendship;
import ute.nhom27.android.model.GroupMessage;
import ute.nhom27.android.model.TypingStatus;
import ute.nhom27.android.model.response.MessageListResponse;
import ute.nhom27.android.model.response.UserResponse;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    @GET("users/{userId}/messages")
    Call<List<ChatMessage>> getMessages(@Path("userId") Long userId);

    @POST("messages")
    Call<ChatMessage> sendMessage(@Body ChatMessage message);

    @GET("groups/{userId}")
    Call<List<ChatGroup>> getGroups(@Path("userId") Long userId);

    @GET("groups/{groupId}/messages")
    Call<List<GroupMessage>> getGroupMessages(@Path("groupId") Long groupId);

    @POST("group-messages")
    Call<GroupMessage> sendGroupMessage(@Body GroupMessage message);

    @GET("api/messages/friends/{userId}")
    Call<List<MessageListResponse>> getFriendLastMessages(@Path("userId") Long userId);

    @GET("friends/{userId}")
    Call<List<Friendship>> getFriendships(@Path("userId") Long userId);
    @GET("api/friends/{userId}/list")
    Call<List<UserResponse>> getFriends(@Path("userId") Long userId);

    @POST("friends")
    Call<Friendship> addFriend(@Body Friendship friendship);

    @GET("typing-status/{userId}")
    Call<List<TypingStatus>> getTypingStatus(@Path("userId") Long userId);

    @POST("ai-suggestions")
    Call<AISuggestion> getAISuggestion(@Body AISuggestionRequest request);
}
