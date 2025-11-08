package com.prm392.g5.labverse.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.dao.ReadingListDao;
import com.prm392.g5.labverse.dao.ReadingListPaperDao;
import com.prm392.g5.labverse.dao.PaperDao;
import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.entity.ReadingList;
import com.prm392.g5.labverse.entity.ReadingListPaper;

import java.util.List;

public class ReadingListRepository {

    private ReadingListDao readingListDao;
    private ReadingListPaperDao readingListPaperDao;
    private PaperDao paperDao;
    private LiveData<List<ReadingList>> allReadingLists;

    public ReadingListRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.readingListDao = db.readingListDao();
        this.readingListPaperDao = db.readingListPaperDao();
        this.paperDao = db.paperDao();
        this.allReadingLists = readingListDao.getAllReadingLists();
    }

    public ReadingListRepository(ReadingListDao readingListDao) {
        this.readingListDao = readingListDao;
        this.allReadingLists = readingListDao.getAllReadingLists();
    }

    public void insert(ReadingList readingList) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = readingListDao.insert(readingList);
            // Update paper count after insert
            int paperCount = readingListDao.getPaperCount(id);
            readingList.setPaperCount(paperCount);
            readingListDao.update(readingList);
        });
    }

    public void update(ReadingList readingList) {
        AppDatabase.databaseWriteExecutor.execute(() -> readingListDao.update(readingList));
    }

    public void delete(ReadingList readingList) {
        AppDatabase.databaseWriteExecutor.execute(() -> readingListDao.delete(readingList));
    }

    public LiveData<List<ReadingList>> getAllReadingLists() {
        return allReadingLists;
    }

    public LiveData<List<ReadingList>> getAllReadingListsSortedByName() {
        return readingListDao.getAllReadingListsSortedByName();
    }

    public LiveData<List<ReadingList>> getAllReadingListsSortedByDate() {
        return readingListDao.getAllReadingListsSortedByDate();
    }

    public LiveData<ReadingList> getReadingListById(long id) {
        return readingListDao.getReadingListById(id);
    }

    public LiveData<List<Paper>> getPapersInReadingList(long readingListId) {
        return paperDao.getPapersInReadingList(readingListId);
    }

    public LiveData<List<Paper>> getAllPapers() {
        return paperDao.getAllPapers();
    }

    public void addPaperToReadingList(ReadingListPaper readingListPaper) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            readingListPaperDao.insert(readingListPaper);
            // Update paper count
            int paperCount = readingListDao.getPaperCount(readingListPaper.getReadingListId());
            ReadingList readingList = readingListDao.getReadingListById(readingListPaper.getReadingListId()).getValue();
            if (readingList != null) {
                readingList.setPaperCount(paperCount);
                readingListDao.update(readingList);
            }
        });
    }

    public void removePaperFromReadingList(long readingListId, String paperId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            readingListPaperDao.deletePaperFromReadingList(readingListId, paperId);
            // Update paper count
            int paperCount = readingListDao.getPaperCount(readingListId);
            ReadingList readingList = readingListDao.getReadingListById(readingListId).getValue();
            if (readingList != null) {
                readingList.setPaperCount(paperCount);
                readingListDao.update(readingList);
            }
        });
    }
}

