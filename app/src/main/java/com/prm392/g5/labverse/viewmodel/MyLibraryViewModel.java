package com.prm392.g5.labverse.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;

import com.prm392.g5.labverse.entity.PaperCache;
import com.prm392.g5.labverse.repository.PaperRepository;

import java.util.List;

public class MyLibraryViewModel extends AndroidViewModel {

    private final PaperRepository repo;
    private final MutableLiveData<String> filter = new MutableLiveData<>("recently_added");
    private final MutableLiveData<String> userId = new MutableLiveData<>();
    private final LiveData<List<PaperCache>> papers;

    public MyLibraryViewModel(@NonNull Application app) {
        super(app);
        repo = new PaperRepository(app.getApplicationContext());
        papers = Transformations.switchMap(filter, f -> repo.observe(f));
    }

    public LiveData<List<PaperCache>> getPapers() { return papers; }
    public void initUser(String uid){ if (userId.getValue()==null) userId.setValue(uid); }

    public void setFilter(String f) {
        filter.setValue(f);
        refresh();
    }

    public void refresh() {
        String f = filter.getValue()==null ? "recently_added" : filter.getValue();
        String uid = userId.getValue();
        if (uid != null) repo.sync(uid, f);
    }
}
