package ute.nhom27.android.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ute.nhom27.android.data.entity.ChatMessage;

@Dao
public interface ChatMessageDao {
    @Insert
    void insert(ChatMessage message);

    @Query("SELECT * FROM chat_messages WHERE (senderId = :userId OR receiverId = :userId) AND isRevoked = 0")
    List<ChatMessage> getMessagesForUser(Long userId);

    @Query("SELECT * FROM chat_messages WHERE content LIKE :query AND (senderId = :userId OR receiverId = :userId)")
    List<ChatMessage> searchMessages(Long userId, String query);
}
