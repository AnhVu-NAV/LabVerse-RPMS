package com.prm392.g5.labverse.config;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.prm392.g5.labverse.dao.PaperAnnotationDao;
import com.prm392.g5.labverse.dao.PaperDao;
import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.entity.PaperAnnotation;
import com.prm392.g5.labverse.util.LocalDateTimeConverter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Paper.class, PaperAnnotation.class}, version = 1, exportSchema = false)
@TypeConverters({LocalDateTimeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private final static String DATABASE_NAME = "LabVerse.db";

    public abstract PaperDao paperDao();
    public abstract PaperAnnotationDao paperAnnotationDao();

    // Thread pool cho tác vụ ghi DB
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(5);

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
