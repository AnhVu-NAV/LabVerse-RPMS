package com.prm392.g5.labverse.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.prm392.g5.labverse.entity.Paper;

@Dao
public interface PaperDao {

    @Insert
    void insert(Paper paper);

    @Update
    void update(Paper paper);

    @Query("SELECT * FROM paper WHERE id LIKE :id")
    Paper getById(String id);

}
