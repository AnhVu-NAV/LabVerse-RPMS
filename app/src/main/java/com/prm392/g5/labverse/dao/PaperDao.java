package com.prm392.g5.labverse.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.prm392.g5.labverse.entity.Paper;

import java.util.List;

@Dao
public interface PaperDao {

    @Insert
    void insert(Paper paper);

    @Update
    void update(Paper paper);

    @Query("SELECT * FROM paper WHERE id LIKE :id")
    Paper getById(String id);

    @Query("SELECT p.* FROM paper p " +
           "INNER JOIN reading_list_paper rlp ON p.id = rlp.paperId " +
           "WHERE rlp.readingListId = :readingListId " +
           "ORDER BY rlp.addedAt DESC")
    LiveData<List<Paper>> getPapersInReadingList(long readingListId);

    @Query("SELECT * FROM paper WHERE deleteFlag = 0 ORDER BY id DESC")
    LiveData<List<Paper>> getAllPapers();

}
