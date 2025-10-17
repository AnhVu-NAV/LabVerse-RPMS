package com.prm392.g5.labverse.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(tableName = "paper")
public class Paper {

    @PrimaryKey
    @NonNull
    private String id;
    private String s3Key;
    private boolean deleteFlag = false;
    private String annotationS3Key;
    private LocalDateTime annotationUpdateAt;
    private String userId;
    private int lastPage = -1;
    private int totalPage = -1;

    //todo paper vẫn còn thiếu thong tin nhe
    public Paper() {
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public boolean isDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public String getAnnotationS3Key() {
        return annotationS3Key;
    }

    public void setAnnotationS3Key(String annotationS3Key) {
        this.annotationS3Key = annotationS3Key;
    }

    public LocalDateTime getAnnotationUpdateAt() {
        return annotationUpdateAt;
    }

    public void setAnnotationUpdateAt(LocalDateTime annotationUpdateAt) {
        this.annotationUpdateAt = annotationUpdateAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getLastPage() {
        return lastPage;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }
}
