package ute.nhom27.android.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {
    // Định dạng cho timestamp từ server (ISO format)
    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    public static String getRelativeTimeSpan(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "";
        }

        try {
            // Parse timestamp từ server
            LocalDateTime messageTime = LocalDateTime.parse(timestamp, ISO_FORMATTER);
            LocalDateTime now = LocalDateTime.now();

            // Tính khoảng thời gian
            Duration duration = Duration.between(messageTime, now);

            long minutes = duration.toMinutes();
            long hours = duration.toHours();
            long days = duration.toDays();

            // Xử lý các trường hợp hiển thị
            if (days > 7) {
                // Nếu quá 7 ngày, hiển thị ngày tháng
                return messageTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else if (days > 0) {
                return days + " ngày trước";
            } else if (hours > 0) {
                return hours + " giờ trước";
            } else if (minutes > 0) {
                return minutes + " phút trước";
            } else {
                return "Vừa xong";
            }

        } catch (DateTimeParseException e) {
            // Log lỗi nếu cần
            e.printStackTrace();

            // Thử parse với format khác nếu format chính không thành công
            try {
                // Format backup không có microseconds
                DateTimeFormatter backupFormatter =
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                LocalDateTime messageTime = LocalDateTime.parse(timestamp, backupFormatter);
                // ... xử lý tương tự như trên

            } catch (DateTimeParseException ex) {
                ex.printStackTrace();
                return "";
            }
            return "";
        }
    }

    // Helper method để format ngày giờ theo định dạng cụ thể
    public static String formatDateTime(String timestamp, String pattern) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, ISO_FORMATTER);
            return dateTime.format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}