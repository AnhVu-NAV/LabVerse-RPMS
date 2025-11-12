package com.prm392.g5.labverse.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "papers_cache")
public class PaperCache {
    @PrimaryKey @NonNull public String id;
    public String title;
    public String authors;
    public String journal;
    public String status;        // UNREAD/READING/FINISHED
    public int    progress;      // %
    public boolean favorite;
    public long   createdAtEpoch;
    public Long   lastReadAtEpoch; // nullable
    String localPath;   // where pdf saved
    long fileSize;
    String fileHash;
}
