package com.prm392.g5.labverse.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.time.LocalDateTime;

@Entity(tableName = "reading_list_paper",
        foreignKeys = {
            @ForeignKey(entity = ReadingList.class,
                    parentColumns = "id",
                    childColumns = "readingListId",
                    onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Paper.class,
                    parentColumns = "id",
                    childColumns = "paperId",
                    onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("readingListId"), @Index("paperId")})
public class ReadingListPaper {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long readingListId;

    @NonNull
    private String paperId;

    private LocalDateTime addedAt;

    public ReadingListPaper() {
        this.addedAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getReadingListId() {
        return readingListId;
    }

    public void setReadingListId(long readingListId) {
        this.readingListId = readingListId;
    }

    @NonNull
    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(@NonNull String paperId) {
        this.paperId = paperId;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}

