package com.prm392.g5.labverse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.entity.ReadingListPaper;
import com.prm392.g5.labverse.repository.ReadingListRepository;

import java.util.List;

public class ReadingListDetailViewModel extends AndroidViewModel {

    private final ReadingListRepository repository;
    private LiveData<List<Paper>> papersInReadingList;

    public ReadingListDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new ReadingListRepository(application);
    }

    public void setReadingListId(long readingListId) {
        papersInReadingList = repository.getPapersInReadingList(readingListId);
    }

    public LiveData<List<Paper>> getPapersInReadingList() {
        return papersInReadingList;
    }

    public LiveData<List<Paper>> getAllPapers() {
        return repository.getAllPapers();
    }

    public void addPaperToReadingList(long readingListId, String paperId) {
        ReadingListPaper readingListPaper = new ReadingListPaper();
        readingListPaper.setReadingListId(readingListId);
        readingListPaper.setPaperId(paperId);
        repository.addPaperToReadingList(readingListPaper);
    }

    public void removePaperFromReadingList(long readingListId, String paperId) {
        repository.removePaperFromReadingList(readingListId, paperId);
    }
}

