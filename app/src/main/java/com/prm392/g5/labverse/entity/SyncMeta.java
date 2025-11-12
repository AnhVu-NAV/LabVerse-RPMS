package com.prm392.g5.labverse.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "sync_meta", primaryKeys = {"userId","key"})
public class SyncMeta {
    @NonNull
    public String userId;
    @NonNull public String key; // e.g., "lastDeltaSyncAt", "lastFullSyncAt"
    public String value;        // ISO8601

    public SyncMeta() {
    }

    public SyncMeta(@NonNull String userId, @NonNull String key, String value) {
        this.userId = userId;
        this.key = key;
        this.value = value;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
