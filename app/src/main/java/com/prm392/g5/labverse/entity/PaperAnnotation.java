package com.prm392.g5.labverse.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Instant;
import java.util.UUID;

@Entity(
        tableName = "paper_annotation",
        indices = {@Index(value = {"user_id", "paper_id"}, unique = true)}
)
public class PaperAnnotation {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "paper_id")
    public String paperId;

    private String annotationS3Key;

    @ColumnInfo(name = "updated_at")
    public String updatedAt; // ISO 8601 string 2025-10-17T08:05:23Z
    boolean deleteFlag;
    long version;
    @NonNull String syncState; // CLEAN / DIRTY / CONFLICT
    private String ownerUserId;

    public PaperAnnotation() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId() {
        this.id = UUID.randomUUID().toString();
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }


    public String getAnnotationS3Key() {
        return annotationS3Key;
    }

    public void setAnnotationS3Key(String annotationS3Key) {
        this.annotationS3Key = annotationS3Key;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt() {
        this.updatedAt = Instant.now().toString();
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
