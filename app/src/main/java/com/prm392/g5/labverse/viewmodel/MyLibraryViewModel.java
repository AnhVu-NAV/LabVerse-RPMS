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

    // papers sẽ theo dõi cả userId lẫn filter
    private final LiveData<List<PaperCache>> papers;

    public MyLibraryViewModel(@NonNull Application app) {
        super(app);
        repo = new PaperRepository(app.getApplicationContext());

        this.papers = Transformations.switchMap(userId, uid ->
                Transformations.switchMap(filter, f ->
                        repo.observe(uid, f) // <— truyền cả uid + filter xuống repo
                )
        );
    }

    public LiveData<List<PaperCache>> getPapers() { return papers; }

    // gọi 1 lần sau login
    public void initUser(String uid){
        if (uid != null && (userId.getValue() == null || !uid.equals(userId.getValue()))) {
            userId.setValue(uid);
            refresh(); // kéo dữ liệu mới về Room ngay lần đầu
        }
    }

    public void setFilter(String f) {
        if (f == null) f = "recently_added";
        if (!f.equals(filter.getValue())) {
            filter.setValue(f);
            refresh(); // đổi tab là sync (nếu online)
        }
    }

    /** Gọi BE để sync về Room (nếu có mạng), UI vẫn dùng LiveData từ Room */
    public void refresh() {
        String uid = userId.getValue();
        String f = filter.getValue() == null ? "recently_added" : filter.getValue();
        if (uid != null) repo.sync(uid, f);
    }
}
