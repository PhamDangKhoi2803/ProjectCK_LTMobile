package ute.nhom27.android.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getRelativeTimeSpan(String timestamp) {
        try {
            LocalDateTime messageTime = LocalDateTime.parse(timestamp, formatter);
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(messageTime, now);

            long hours = duration.toHours();
            long days = duration.toDays();

            if (days > 0) {
                return days + " ngày trước";
            } else if (hours > 0) {
                return hours + " giờ trước";
            } else {
                long minutes = duration.toMinutes();
                if (minutes <= 1) {
                    return "Vừa xong";
                } else {
                    return minutes + " phút trước";
                }
            }
        } catch (Exception e) {
            return "";
        }
    }
} 