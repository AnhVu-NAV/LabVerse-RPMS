package com.prm392.g5.labverse.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.prm392.g5.labverse.entity.PaperCache;
import java.util.List;

@Dao
public interface PaperDashboardDao {

    @Query("SELECT * FROM papers_cache ORDER BY createdAtEpoch DESC")
    LiveData<List<PaperCache>> recentlyAdded();

    @Query("SELECT * FROM papers_cache ORDER BY lastReadAtEpoch DESC")
    LiveData<List<PaperCache>> recentlyRead();

    @Query("SELECT * FROM papers_cache WHERE favorite = 1 ORDER BY createdAtEpoch DESC")
    LiveData<List<PaperCache>> favorites();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<PaperCache> items);

    // Recently added = sort theo createdAt mới → cũ
    @Query("SELECT * FROM papers_cache " +
            "WHERE userId = :userId " +
            "ORDER BY createdAtEpoch DESC, title COLLATE NOCASE")
    LiveData<List<PaperCache>> observeRecentlyAdded(String userId);

    // Recently read = có lastReadAtEpoch, sort theo lastReadAt mới → cũ
    @Query("SELECT * FROM papers_cache " +
            "WHERE userId = :userId AND lastReadAtEpoch IS NOT NULL " +
            "ORDER BY lastReadAtEpoch DESC, title COLLATE NOCASE")
    LiveData<List<PaperCache>> observeRecentlyRead(String userId);

    // Favorites = favorite = 1, sort theo createdAt mới → cũ (hoặc lastReadAt tuỳ bạn)
    @Query("SELECT * FROM papers_cache " +
            "WHERE userId = :userId AND favorite = 1 " +
            "ORDER BY createdAtEpoch DESC, title COLLATE NOCASE")
    LiveData<List<PaperCache>> observeFavorites(String userId);

    // Tiện cho debug/refresh
    @Query("DELETE FROM papers_cache WHERE userId = :userId")
    void clearForUser(String userId);

    @Query("UPDATE papers_cache SET pendingDownload = 1 WHERE id = :paperId")
    void markPendingDownload(String paperId);

    @Query("UPDATE papers_cache SET pendingDownload = 0 WHERE id = :paperId")
    void clearPendingDownload(String paperId);

    @Query("UPDATE papers_cache SET localPath = :path WHERE id = :paperId")
    void updateLocalPath(String paperId, String path);

    // Prefetch “recent” lấy từ cache (không cần chạm Paper table)
    @Query("""
        SELECT * FROM papers_cache
        WHERE userId = :userId
        ORDER BY 
          CASE WHEN lastReadAtEpoch IS NOT NULL THEN lastReadAtEpoch ELSE createdAtEpoch END DESC
        LIMIT :limit
    """)
    List<PaperCache> getRecentCacheNow(String userId, int limit);
}
