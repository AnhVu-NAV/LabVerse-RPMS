package com.prm392.g5.labverse.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.prm392.g5.labverse.entity.PaperAnnotation;

@Dao
public interface PaperAnnotationDao {
    @Insert
    void insert (PaperAnnotation paperAnnotation);

    @Update
    void update (PaperAnnotation paperAnnotation);

    @Query("SELECT * FROM paper_annotation WHERE paper_id = :paperId AND user_id = :userId LIMIT 1")
    PaperAnnotation getAnnotationByPaperIdAndUserId(String paperId, String userId);
    @Query("SELECT * FROM paper_annotation WHERE id = :annotationId")
    PaperAnnotation getAnnotationById(String annotationId);
}
