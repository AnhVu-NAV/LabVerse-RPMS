package com.prm392.g5.labverse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.dao.ReadingListDao;
import com.prm392.g5.labverse.entity.ReadingList;
import com.prm392.g5.labverse.repository.ReadingListRepository;

import java.util.List;

public class ReadingListViewModel extends AndroidViewModel {

    private ReadingListRepository repository;
    private LiveData<List<ReadingList>> allReadingLists;

    public ReadingListViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        ReadingListDao readingListDao = database.readingListDao();
        repository = new ReadingListRepository(readingListDao);
        allReadingLists = repository.getAllReadingLists();
    }

    public void insert(ReadingList readingList) {
        repository.insert(readingList);
    }

    public void update(ReadingList readingList) {
        repository.update(readingList);
    }

    public void delete(ReadingList readingList) {
        repository.delete(readingList);
    }

    public LiveData<List<ReadingList>> getAllReadingLists() {
        return allReadingLists;
    }

    public LiveData<List<ReadingList>> getAllReadingListsSortedByName() {
        return repository.getAllReadingListsSortedByName();
    }

    public LiveData<List<ReadingList>> getAllReadingListsSortedByDate() {
        return repository.getAllReadingListsSortedByDate();
    }

    public LiveData<ReadingList> getReadingListById(long id) {
        return repository.getReadingListById(id);
    }
}

