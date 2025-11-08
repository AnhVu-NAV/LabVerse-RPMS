package com.prm392.g5.labverse.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.prm392.g5.labverse.entity.ReadingListPaper;

import java.util.List;

@Dao
public interface ReadingListPaperDao {

    @Insert
    long insert(ReadingListPaper readingListPaper);

    @Delete
    void delete(ReadingListPaper readingListPaper);

    @Query("SELECT * FROM reading_list_paper WHERE readingListId = :readingListId ORDER BY addedAt DESC")
    LiveData<List<ReadingListPaper>> getPapersInReadingList(long readingListId);

    @Query("DELETE FROM reading_list_paper WHERE readingListId = :readingListId AND paperId = :paperId")
    void deletePaperFromReadingList(long readingListId, String paperId);

    @Query("SELECT COUNT(*) FROM reading_list_paper WHERE readingListId = :readingListId")
    LiveData<Integer> getPaperCountLive(long readingListId);
}

