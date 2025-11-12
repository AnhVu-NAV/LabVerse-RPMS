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
}
