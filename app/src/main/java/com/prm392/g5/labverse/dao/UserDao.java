package com.prm392.g5.labverse.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.prm392.g5.labverse.entity.UserEntity;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    LiveData<UserEntity> getUserProfile();

    @Query("SELECT * FROM user_profile LIMIT 1")
    UserEntity getUserNow(); // sync (cho repo offline-first)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(UserEntity user);

    @Update
    void update(UserEntity user);

    @Query("UPDATE user_profile SET pending_update = :pending WHERE id = :id")
    void markPending(String id, boolean pending);

    @Query("DELETE FROM user_profile")
    void clearUser();
}
