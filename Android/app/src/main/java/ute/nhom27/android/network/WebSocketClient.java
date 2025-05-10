package ute.nhom27.android.network;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketClient {
    private OkHttpClient client;
    private WebSocket webSocket;
    private final String WS_URL = "ws://your-backend-url/ws";

    public WebSocketClient() {
        client = new OkHttpClient();
    }

    public void connect(WebSocketListener listener) {
        Request request = new Request.Builder().url(WS_URL).build();
        webSocket = client.newWebSocket(request, listener);
    }

    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    public void sendTypingStatus(Long userId, Long receiverId, Long groupId) {
        // Gửi sự kiện typing qua WebSocket
        JSONObject json = new JSONObject();
        try {
            json.put("userId", userId);
            json.put("receiverId", receiverId);
            json.put("groupId", groupId);
            json.put("type", "TYPING");
            webSocket.send(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Disconnected");
        }
    }
}