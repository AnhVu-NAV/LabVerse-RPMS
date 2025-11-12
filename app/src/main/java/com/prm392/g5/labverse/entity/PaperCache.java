package com.prm392.g5.labverse.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "papers_cache",
primaryKeys = {"userId", "id"},
indices = {
@Index("userId"),
@Index("createdAtEpoch"),
@Index("lastReadAtEpoch"),
@Index("favorite")
    })
public class PaperCache {
    @NonNull public String id;
    public String title;
    public String authors;
    public String journal;
    public String status;        // UNREAD/READING/FINISHED
    public int    progress;      // %
    public boolean favorite;
    public long   createdAtEpoch;
    public Long   lastReadAtEpoch; // nullable
    public String localPath;   // where pdf saved
    public long fileSize;
    public String fileHash;
    @NonNull
    public String userId;

    public int pendingDownload;
}
