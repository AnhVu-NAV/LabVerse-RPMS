package com.prm392.g5.labverse.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.prm392.g5.labverse.entity.SyncMeta;

@Dao
public interface SyncMetaDao {

    // Trả về đúng cột 'value' → Room mới convert về String được
    @Query("SELECT value FROM sync_meta WHERE userId = :userId AND `key` = :key LIMIT 1")
    String getValue(String userId, String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SyncMeta meta);
}