package ute.nhom27.android.model.response;

import android.icu.text.SimpleDateFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MessageResponse {
    private Long senderId;
    private Long receiverId;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private String timestamp;
    private String status;
    private boolean isGroup;

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getFormattedTime() {
        try {
            if (timestamp != null) {
                SimpleDateFormat inputFormat =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                SimpleDateFormat outputFormat =
                        new SimpleDateFormat("HH:mm");

                Date date = inputFormat.parse(timestamp);
                return outputFormat.format(date);
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }
}
