package com.prm392.g5.labverse.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
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
    LiveData<List<Paper>> getPapersInReadingList(String readingListId);

    @Query("SELECT * FROM paper WHERE deleteFlag = 0 ORDER BY id DESC")
    LiveData<List<Paper>> getAllPapers();

//    @Query("""
//        SELECT DISTINCT p.*
//        FROM paper p
//        LEFT JOIN reading_list_paper rlp ON rlp.paperId = p.id
//        LEFT JOIN reading_list rl ON rl.id = rlp.readingListId
//        WHERE (:userId IS NULL OR rl.userId = :userId)
//
//          AND (:kw IS NULL OR
//               p.title       LIKE :kw  COLLATE NOCASE OR
//               p.authorName  LIKE :kw  COLLATE NOCASE OR
//               p.journalName LIKE :kw  COLLATE NOCASE)
//
//          AND (:author  IS NULL OR p.authorName   LIKE :author  COLLATE NOCASE)
//          AND (:journal IS NULL OR p.journalName  LIKE :journal COLLATE NOCASE)
//          AND (:tag     IS NULL OR p.title        LIKE :tag     COLLATE NOCASE)
//          AND (:year    IS NULL OR p.publicationYear LIKE :year)
//
//        ORDER BY p.publicationYear DESC, p.title COLLATE NOCASE
//    """)
//    List<Paper> searchAdvancedOwned(
//            String userId, String kw, String author, String journal, String tag, String year
//    );

    @Query("""
        SELECT *
        FROM paper p
        WHERE (:kw IS NULL OR 
               p.title       LIKE :kw  COLLATE NOCASE OR
               p.authorName  LIKE :kw  COLLATE NOCASE OR
               p.journalName LIKE :kw  COLLATE NOCASE)
          AND (:author  IS NULL OR p.authorName   LIKE :author  COLLATE NOCASE)
          AND (:journal IS NULL OR p.journalName  LIKE :journal COLLATE NOCASE)
          AND (:tag     IS NULL OR p.title        LIKE :tag     COLLATE NOCASE)
          AND (:year    IS NULL OR p.publicationYear LIKE :year)
        ORDER BY p.publicationYear DESC, p.title COLLATE NOCASE
    """)
    List<Paper> searchAdvanced(
            String kw, String author, String journal, String tag, String year
    );

    @Query("SELECT COUNT(*) FROM paper")
    int countAllNow();

    @Query("""
     SELECT * FROM paper
     WHERE ownerUserId = :userId AND deleteFlag = 0
     ORDER BY updatedAt DESC
  """)
    LiveData<List<Paper>> getAllLive(String userId);

    @Query("""
     SELECT * FROM paper
     WHERE ownerUserId = :userId AND deleteFlag = 0
     ORDER BY updatedAt DESC
     LIMIT :limit
  """)
    List<Paper> getRecentNow(String userId, int limit);

    @Query("""
     SELECT DISTINCT p.* FROM paper p
     LEFT JOIN reading_list_paper rlp ON rlp.paperId = p.id
     LEFT JOIN reading_list rl ON rl.id = rlp.readingListId
     WHERE p.ownerUserId = :userId
       AND p.deleteFlag = 0
       AND (:kw IS NULL OR
            p.title       LIKE :kw ESCAPE '\' COLLATE NOCASE OR
            p.authorName  LIKE :kw ESCAPE '\' COLLATE NOCASE OR
            p.journalName LIKE :kw ESCAPE '\' COLLATE NOCASE)
       AND (:author  IS NULL OR p.authorName   LIKE :author  ESCAPE '\' COLLATE NOCASE)
       AND (:journal IS NULL OR p.journalName  LIKE :journal ESCAPE '\' COLLATE NOCASE)
       AND (:tag     IS NULL OR p.title        LIKE :tag     ESCAPE '\' COLLATE NOCASE) 
       AND (:year    IS NULL OR p.publicationYear LIKE :year)
     ORDER BY p.updatedAt DESC, p.title COLLATE NOCASE
  """)
    List<Paper> searchAdvancedOwned(
            String userId, String kw, String author, String journal, String tag, String year
    );

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<Paper> papers);

}
