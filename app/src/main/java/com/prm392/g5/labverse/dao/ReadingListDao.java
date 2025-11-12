package com.prm392.g5.labverse.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.prm392.g5.labverse.entity.ReadingList;

import java.util.List;

@Dao
public interface ReadingListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ReadingList rl);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ReadingList> lists);

    @Update
    void update(ReadingList rl);

    @Delete
    void delete(ReadingList rl);

    @Query("SELECT * FROM reading_list ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<ReadingList>> getAllReadingListsSortedByName();

    @Query("SELECT * FROM reading_list ORDER BY createdAt DESC")
    LiveData<List<ReadingList>> getAllReadingListsSortedByDate();

    @Query("SELECT * FROM reading_list")
    LiveData<List<ReadingList>> getAllReadingLists();

    // CHANGED: id String
    @Query("SELECT * FROM reading_list WHERE id = :id LIMIT 1")
    LiveData<ReadingList> getReadingListById(String id);

    // Hỗ trợ cập nhật paperCount (dùng trong repository)
    @Query("SELECT * FROM reading_list WHERE id = :id LIMIT 1")
    ReadingList blockingFindById(String id);

    // Nếu bạn lưu table mapping list-paper, hãy đổi tham số sang String
    @Query("SELECT COUNT(*) FROM reading_list_paper WHERE readingListId = :listId")
    Integer getPaperCount(String listId);

    @Query("""
        SELECT * FROM reading_list
        WHERE (:kw IS NULL OR name LIKE :kw COLLATE NOCASE)
        ORDER BY createdAt DESC
    """)
    List<ReadingList> searchByName(String kw);

    @Query("""
     SELECT * FROM reading_list
     WHERE ownerUserId = :userId AND deleteFlag = 0
     ORDER BY updatedAt DESC
  """)
    LiveData<List<ReadingList>> getAllLive(String userId);

    @Query("""
     SELECT * FROM reading_list
     WHERE ownerUserId = :userId AND deleteFlag = 0
     ORDER BY updatedAt DESC
     LIMIT :limit
  """)
    List<ReadingList> getRecentNow(String userId, int limit);

}
