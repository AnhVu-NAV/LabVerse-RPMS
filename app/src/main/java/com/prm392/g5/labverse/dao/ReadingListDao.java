package com.prm392.g5.labverse.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.prm392.g5.labverse.entity.ReadingList;

import java.util.List;

@Dao
public interface ReadingListDao {

    @Insert
    long insert(ReadingList readingList);

    @Update
    void update(ReadingList readingList);

    @Delete
    void delete(ReadingList readingList);

    @Query("SELECT * FROM reading_list ORDER BY createdAt DESC")
    LiveData<List<ReadingList>> getAllReadingLists();

    @Query("SELECT * FROM reading_list ORDER BY name ASC")
    LiveData<List<ReadingList>> getAllReadingListsSortedByName();

    @Query("SELECT * FROM reading_list ORDER BY createdAt DESC")
    LiveData<List<ReadingList>> getAllReadingListsSortedByDate();

    @Query("SELECT * FROM reading_list WHERE id = :id")
    LiveData<ReadingList> getReadingListById(long id);

    @Query("SELECT COUNT(*) FROM reading_list_paper WHERE readingListId = :readingListId")
    int getPaperCount(long readingListId);
}

