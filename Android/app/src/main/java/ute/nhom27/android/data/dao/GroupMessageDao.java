package ute.nhom27.android.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ute.nhom27.android.data.entity.GroupMessage;

@Dao
public interface GroupMessageDao {
    @Insert
    void insert(GroupMessage message);

    @Query("SELECT * FROM group_messages WHERE chatGroupId = :groupId AND isRevoked = 0")
    List<GroupMessage> getMessagesForGroup(Long groupId);

    @Query("SELECT * FROM group_messages WHERE content LIKE :query AND chatGroupId = :groupId")
    List<GroupMessage> searchMessages(Long groupId, String query);
}
