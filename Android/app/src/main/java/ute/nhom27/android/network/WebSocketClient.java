package ute.nhom27.android.network;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;
import ute.nhom27.android.model.response.NotificationResponse;
import ute.nhom27.android.utils.SharedPrefManager;

public class WebSocketClient {
    private StompClient stompClient;
    private final String userId;
    private final String token;
    private final String WS_URL = "ws://10.0.2.2:8081/chat/websocket"; // Xác nhận port
    private final Gson gson = new Gson();
    private final OnMessageReceivedListener messageListener;

    public WebSocketClient(Context context, OnMessageReceivedListener listener) {
        SharedPrefManager sharedPrefManager = new SharedPrefManager(context);
        this.userId = String.valueOf(sharedPrefManager.getUser().getId());
        this.token = sharedPrefManager.getToken();
        Log.d("WebSocket", "Token: " + token);
        this.messageListener = listener;

        if (userId.equals("0") || token == null) {
            Log.e("WebSocket", "User not logged in or token missing");
            throw new IllegalStateException("User must be logged in to connect WebSocket");
        }
    }

    public void connect() {
        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("Authorization", "Bearer " + token));
        Log.d("WebSocket", "Connecting with token: " + token);

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL);
        stompClient.withClientHeartbeat(10000).withServerHeartbeat(10000);

        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d("WebSocket", "Connection opened");
                    if (messageListener != null) {
                        messageListener.onConnectionStatusChanged(true);
                    }
                    break;
                case ERROR:
                    Log.e("WebSocket", "Connection error: " + lifecycleEvent.getException().getMessage());
                    if (messageListener != null) {
                        messageListener.onConnectionStatusChanged(false);
                    }
                    // Thử lại sau 5 giây
                    new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Log.d("WebSocket", "Retrying connection...");
                        stompClient.connect(headers);
                    }, 5000);
                    break;
                case CLOSED:
                    Log.d("WebSocket", "Connection closed");
                    if (messageListener != null) {
                        messageListener.onConnectionStatusChanged(false);
                    }
                    break;
                case FAILED_SERVER_HEARTBEAT:
                    Log.e("WebSocket", "Server heartbeat failed");
                    break;
            }
        });

        stompClient.connect(headers);

        // Đăng ký kênh tin nhắn
        stompClient.topic("/user/" + userId + "/topic/messages").subscribe(topicMessage -> {
            String text = topicMessage.getPayload();
            Log.d("WebSocket", "Message received: " + text);
            try {
                JSONObject json = new JSONObject(text);
                String type = json.getString("type");
                switch (type) {
                    case "MESSAGE":
                        if (messageListener != null) {
                            messageListener.onMessageReceived(text);
                        }
                        break;
                    case "TYPING":
                        if (messageListener != null) {
                            messageListener.onTypingStatusReceived(text);
                        }
                        break;
                    case "ONLINE_STATUS":
                        if (messageListener != null) {
                            // Parse JSON thành NotificationResponse nếu cần
                            NotificationResponse notification = gson.fromJson(text, NotificationResponse.class);
                            messageListener.onNotificationReceived(notification);
                        }
                        break;
                    default:
                        Log.w("WebSocket", "Unknown message type: " + type);
                }
            } catch (JSONException e) {
                Log.e("WebSocket", "Error parsing message: " + e.getMessage());
            }
        }, throwable -> {
            Log.e("WebSocket", "Error in messages: " + throwable.getMessage());
        });

        // Đăng ký kênh thông báo
        stompClient.topic("/user/" + userId + "/topic/notifications").subscribe(topicMessage -> {
            String notification = topicMessage.getPayload();
            Log.d("WebSocket", "Notification received: " + notification);
            if (messageListener != null) {
                try {
                    NotificationResponse notificationResponse = gson.fromJson(notification, NotificationResponse.class);
                    messageListener.onNotificationReceived(notificationResponse);
                } catch (Exception e) {
                    Log.e("WebSocket", "Error parsing notification: " + e.getMessage());
                }
            }
        }, throwable -> {
            Log.e("WebSocket", "Error in notifications: " + throwable.getMessage());
        });

        // Đăng ký kênh typing
        stompClient.topic("/user/" + userId + "/topic/typing").subscribe(topicMessage -> {
            String typing = topicMessage.getPayload();
            Log.d("WebSocket", "Typing status received: " + typing);
            if (messageListener != null) {
                messageListener.onTypingStatusReceived(typing);
            }
        }, throwable -> {
            Log.e("WebSocket", "Error in typing: " + throwable.getMessage());
        });
    }

    public void sendMessage(String message) {
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.send("/app/chat.sendMessage", message).subscribe(
                    () -> Log.d("WebSocket", "Message sent"),
                    throwable -> Log.e("WebSocket", "Error sending message: " + throwable.getMessage())
            );
        } else {
            Log.e("WebSocket", "Cannot send message: WebSocket not connected");
        }
    }

    public void sendTypingStatus(Long receiverId, Long groupId) {
        if (stompClient != null && stompClient.isConnected()) {
            String typingMessage = "{\"senderId\":" + userId + ",\"receiverId\":" + receiverId + ",\"type\":\"TYPING\",\"content\":\"TYPING\"}";
            stompClient.send("/app/chat.typing", typingMessage).subscribe(
                    () -> Log.d("WebSocket", "Typing status sent"),
                    throwable -> Log.e("WebSocket", "Error sending typing: " + throwable.getMessage())
            );
        } else {
            Log.e("WebSocket", "Cannot send typing status: WebSocket not connected");
        }
    }

    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }
}