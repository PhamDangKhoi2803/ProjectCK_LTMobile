package ute.nhom27.android.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import ute.nhom27.android.data.dao.ChatMessageDao;
import ute.nhom27.android.data.dao.GroupMessageDao;
import ute.nhom27.android.data.dao.UserDao;
import ute.nhom27.android.data.entity.ChatMessage;
import ute.nhom27.android.data.entity.GroupMessage;
import ute.nhom27.android.data.entity.User;

@Database(entities = {User.class, ChatMessage.class, GroupMessage.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ChatMessageDao chatMessageDao();
    public abstract GroupMessageDao groupMessageDao();
}
