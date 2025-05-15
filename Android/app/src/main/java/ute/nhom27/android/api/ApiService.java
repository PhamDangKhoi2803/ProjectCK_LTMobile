package ute.nhom27.android.api;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ute.nhom27.android.model.AISuggestion;
import ute.nhom27.android.model.ChatGroup;
import ute.nhom27.android.model.ChatMessage;
import ute.nhom27.android.model.Friendship;
import ute.nhom27.android.model.GroupMessage;
import ute.nhom27.android.model.GroupMember;
import ute.nhom27.android.model.TypingStatus;
import ute.nhom27.android.model.User;
import ute.nhom27.android.model.request.MessageRequest;
import ute.nhom27.android.model.response.GroupMemberResponse;
import ute.nhom27.android.model.response.GroupMessageResponse;
import ute.nhom27.android.model.response.MessageListResponse;
import ute.nhom27.android.model.response.MessageResponse;
import ute.nhom27.android.model.response.UserResponse;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    @PUT("auth/update-theme")
    Call<User> updateTheme(@Body ThemeUpdateRequest request, @Header("Authorization") String token);
    
    @GET("users/{userId}/messages")
    Call<List<ChatMessage>> getMessages(@Path("userId") Long userId);

    @POST("messages")
    Call<ChatMessage> sendMessage(@Body ChatMessage message);
    @GET("api/friends/{userId}/list")
    Call<List<UserResponse>> getFriends(@Path("userId") Long userId);

    @GET("api/friends/{userId}/list-request")
    Call<List<UserResponse>> getFriendRequests(@Path("userId") Long userId);

    @POST("api/friends/accept")
    Call<ResponseBody> acceptFriendRequest(
            @Query("receiverId") Long receiverId,
            @Query("senderId") Long senderId
    );

    @POST("api/friends/reject")
    Call<ResponseBody> rejectFriendRequest(
            @Query("receiverId") Long receiverId,
            @Query("senderId") Long senderId
    );
    @GET("api/messages/private")
    Call<List<MessageResponse>> getPrivateMessages(
            @Query("userId1") Long userId1,
            @Query("userId2") Long userId2
    );
    @GET("api/friends/{userId}/sent-requests")
    Call<List<UserResponse>> getSentFriendRequests(@Path("userId") Long userId);

    @GET("api/friends/{userId}/non-friends")
    Call<List<UserResponse>> getNonFriendUsers(@Path("userId") Long userId);

    @POST("api/friends/remove")
    Call<ResponseBody> removeFriendRequest(
            @Query("senderId") Long senderId,
            @Query("receiverId") Long receiverId
    );

    @POST("api/friends/request")
    Call<ResponseBody> sendFriendRequest(
            @Query("senderId") Long senderId,
            @Query("receiverId") Long receiverId
    );

    @POST("api/friends/unfriend")
    Call<ResponseBody> unfriend(
            @Query("userId") Long userId,
            @Query("friendId") Long friendId
    );

    @GET("api/messages/friends/{userId}")
    Call<List<MessageListResponse>> getFriendLastMessages(@Path("userId") Long userId);

    @GET("api/messages/group/{groupId}")
    Call<List<GroupMessageResponse>> getGroupMessages(@Path("groupId") Long groupId);
    @GET("api/messages/group-last-messages/{userId}")
    Call<List<MessageListResponse>> getGroupLastMessages(@Path("userId") Long userId);

    @POST("api/groups/create")
    Call<Map<String, Object>> createGroup(
            @Query("name") String name,
            @Query("ownerId") Long ownerId
    );

    @POST("api/groups/{groupId}/members/add")
    Call<Map<String, String>> addGroupMember(
            @Path("groupId") Long groupId,
            @Query("userId") Long userId
    );

    @GET("api/groups/user/{userId}")
    Call<List<GroupMemberResponse>> getGroupMembers(@Path("userId") Long userId);

    @GET("api/groups/{groupId}")
    Call<Map<String, Object>> getGroupInfo(@Path("groupId") Long groupId);

    @GET("api/groups/user/{userId}")
    Call<Map<String, Object>> getUserGroups(@Path("userId") Long userId);

    @POST("api/messages/send")
    Call<ResponseBody> sendPrivateMessage(@Body MessageRequest message);

    @POST("api/messages/group/send")
    Call<ResponseBody> sendGroupMessage(@Body MessageRequest message);

//    @POST("api/text-generator")
//    Call<OpenAiResponse> getChatResponse(
//            @Header("Authorization") String authHeader,
//            @Body OpenAiRequest request
//    );

    @POST("v1/generate")
    Call<DeepAIResponse> getChatResponse(
            @Header("Authorization") String authHeader,
            @Body DeepAIRequest request
    );}
