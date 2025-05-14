package ute.nhom27.android.network;

import ute.nhom27.android.model.response.NotificationResponse;

public interface OnMessageReceivedListener {
    void onMessageReceived(String message);
    void onNotificationReceived(NotificationResponse notification);
    void onTypingStatusReceived(String typing);
    void onConnectionStatusChanged(boolean isConnected);
}