package ute.nhom27.android.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import ute.nhom27.android.data.entity.User;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserById(Long userId);
}