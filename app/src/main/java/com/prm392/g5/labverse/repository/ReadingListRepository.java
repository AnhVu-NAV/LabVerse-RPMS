package com.prm392.g5.labverse.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prm392.g5.labverse.apiService.ReadingListApi;
import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dao.PaperDao;
import com.prm392.g5.labverse.dao.ReadingListDao;
import com.prm392.g5.labverse.dao.ReadingListPaperDao;
import com.prm392.g5.labverse.dto.readingList.ReadingListResponse;
import com.prm392.g5.labverse.dto.readingList.ReadingListSummaryDTO;
import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.entity.ReadingList;
import com.prm392.g5.labverse.entity.ReadingListPaper;
import com.prm392.g5.labverse.util.MapperUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadingListRepository {

    private final ReadingListDao readingListDao;
    private final ReadingListPaperDao readingListPaperDao;
    private final PaperDao paperDao;
    private final LiveData<List<ReadingList>> allReadingLists;

    // NEW: dùng để đồng bộ với BE
    private final ReadingListApi api = RetrofitClient.getInstance().create(ReadingListApi.class);

    private final MutableLiveData<List<ReadingList>> readingLists = new MutableLiveData<>(new ArrayList<>());


    public ReadingListRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.readingListDao = db.readingListDao();
        this.readingListPaperDao = db.readingListPaperDao();
        this.paperDao = db.paperDao();
        this.allReadingLists = readingListDao.getAllReadingLists();
    }

    // Giữ constructor cũ nếu nơi khác tiêm dao trực tiếp
    public ReadingListRepository(ReadingListDao readingListDao) {
        this.readingListDao = readingListDao;
        this.readingListPaperDao = null;
        this.paperDao = null;
        this.allReadingLists = readingListDao.getAllReadingLists();
    }

    // ===== Room operations (id = String) =====

    // Upsert theo id String (yêu cầu DAO dùng @Insert(onConflict = REPLACE))
    public void upsert(ReadingList readingList) {
        AppDatabase.databaseWriteExecutor.execute(() -> readingListDao.upsert(readingList));
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

    // CHANGED: id -> String
    public LiveData<ReadingList> getReadingListById(String id) {
        return readingListDao.getReadingListById(id);
    }

    // CHANGED: readingListId -> String
    public LiveData<List<Paper>> getPapersInReadingList(String readingListId) {
        return paperDao.getPapersInReadingList(readingListId);
    }

    public LiveData<List<Paper>> getAllPapers() {
        return paperDao.getAllPapers();
    }

    // CHANGED: ReadingListPaper should also use String readingListId
    public void addPaperToReadingList(ReadingListPaper readingListPaper) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            readingListPaperDao.insert(readingListPaper);
            // Nếu cần cập nhật paperCount local, bạn có thể query count theo String id
            Integer paperCount = readingListDao.getPaperCount(readingListPaper.getReadingListId());
            ReadingList rl = readingListDao.blockingFindById(readingListPaper.getReadingListId());
            if (rl != null && paperCount != null) {
                rl.setPaperCount(paperCount);
                readingListDao.update(rl);
            }
        });
    }

    public void removePaperFromReadingList(String readingListId, String paperId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            readingListPaperDao.deletePaperFromReadingList(readingListId, paperId);
            Integer paperCount = readingListDao.getPaperCount(readingListId);
            ReadingList rl = readingListDao.blockingFindById(readingListId);
            if (rl != null && paperCount != null) {
                rl.setPaperCount(paperCount);
                readingListDao.update(rl);
            }
        });
    }

    // ====== Remote sync helpers ======

    // Map từ response BE -> entity Room
    public ReadingList mapRemoteToLocal(ReadingListResponse r) {
        ReadingList e = new ReadingList();
        e.setId(r.id);                                    // String UUID
        e.setName(nz(r.name));
        e.setDescription(nz(r.description));
        try {
            if (r.createdAt != null && !r.createdAt.isEmpty()) {
                e.setCreatedAt(LocalDateTime.parse(r.createdAt));
            } else {
                e.setCreatedAt(LocalDateTime.now());
            }
        } catch (Exception ex) {
            e.setCreatedAt(LocalDateTime.now());
        }
        e.setUpdatedAt(e.getCreatedAt());
        e.setPaperCount(r.paperCount != null ? r.paperCount : 0);
        return e;
    }

    private String nz(String s){ return s == null ? "" : s; }

    // Đồng bộ toàn bộ list của current user từ BE -> Room
    public void refreshFromServer() {
        api.listMine().enqueue(new Callback<List<ReadingListSummaryDTO>>() {
            @Override
            public void onResponse(Call<List<ReadingListSummaryDTO>> call,
                                   Response<List<ReadingListSummaryDTO>> res) {
                if (res.isSuccessful() && res.body() != null) {

                    List<ReadingList> ui = new ArrayList<>();
                    for (ReadingListSummaryDTO dto : res.body()) {
                        ReadingList rl = new ReadingList();
                        rl.setId(dto.getId());
                        rl.setName(dto.getName());
                        rl.setDescription(dto.getDescription());
                        try {
                            if (dto.getCreatedAt() != null) {
                                rl.setCreatedAt(LocalDateTime.parse(dto.getCreatedAt()));
                            }
                        } catch (Exception e) {
                            // nếu parse lỗi thì set now() hoặc bỏ qua
                            rl.setCreatedAt(LocalDateTime.now());
                        }

                        // paperCount BE trả ở dto
                        rl.setPaperCount(dto.getPaperCount()); // nhớ add field này trong entity nếu chưa có

                        ui.add(rl);
                    }

                    readingLists.postValue(ui);
                }
            }

            @Override
            public void onFailure(Call<List<ReadingListSummaryDTO>> call, Throwable t) {
                // TODO log
            }
        });

    }
}
