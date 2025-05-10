package ute.nhom27.android.network;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatWebSocketListener extends WebSocketListener {
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        // Xử lý tin nhắn, typing status, online/offline
        try {
            JSONObject json = new JSONObject(text);
            String type = json.getString("type");
            switch (type) {
                case "MESSAGE":
                    // Cập nhật UI với tin nhắn mới
                    break;
                case "TYPING":
                    // Hiển thị typing indicator
                    break;
                case "ONLINE_STATUS":
                    // Cập nhật trạng thái online/offline
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}