package com.prm392.g5.labverse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prm392.g5.labverse.apiService.ReadingListApi;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dto.readingList.CreateReadingListRequest;
import com.prm392.g5.labverse.dto.readingList.ReadingListResponse;
import com.prm392.g5.labverse.dto.readingList.ReadingListSummaryDTO;
import com.prm392.g5.labverse.dto.readingList.UpdateReadingListRequest;
import com.prm392.g5.labverse.entity.ReadingList;
import com.prm392.g5.labverse.repository.ReadingListRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadingListViewModel extends AndroidViewModel {

    private final ReadingListRepository repository;
    private final LiveData<List<ReadingList>> allReadingLists;

    // NEW: gọi BE trực tiếp (cho create/refresh)
    private final ReadingListApi api;

    private final MutableLiveData<List<ReadingList>> readingLists = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<ReadingList>> getReadingLists() { return readingLists; }

    public ReadingListViewModel(@NonNull Application application) {
        super(application);
        repository = new ReadingListRepository(application);
        allReadingLists = repository.getAllReadingLists();
        api = RetrofitClient.getInstance().create(ReadingListApi.class);
        reload();
    }

    // ===== Local Room (giữ cho tương thích các chỗ đang gọi) =====
    public void insert(ReadingList readingList) {
        // Với id String từ BE, Room nên upsert theo id (REPLACE). Dùng khi cần seed/local.
        repository.upsert(readingList);
    }

    public void update(ReadingList readingList) {
        repository.update(readingList);
    }

    public void delete(ReadingList readingList) {
        // Xoá local + (tuỳ) bạn có thể gọi BE xoá bằng repository.deleteRemote(readingList.getId())
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

    // CHANGED: id -> String
    public LiveData<ReadingList> getReadingListById(@NonNull String id) {
        return repository.getReadingListById(id);
    }

    // ====== RECOMMENDED FLOW: tạo list qua BE để nhận UUID ======

    public void createReadingList(@NonNull String name,
                                  String description,
                                  Runnable onDone,
                                  Consumer<String> onError) {
        CreateReadingListRequest body = new CreateReadingListRequest(name, description);
        api.create(body).enqueue(new Callback<ReadingListResponse>() {
            @Override
            public void onResponse(Call<ReadingListResponse> call, Response<ReadingListResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    // Map về entity local và upsert vào Room + LiveData
                    ReadingList mapped = repository.mapRemoteToLocal(resp.body());
                    repository.upsert(mapped);
                    if (onDone != null) onDone.run();
                } else {
                    if (onError != null) onError.accept("HTTP " + resp.code());
                }
            }

            @Override
            public void onFailure(Call<ReadingListResponse> call, Throwable t) {
                if (onError != null) onError.accept(t.getMessage());
            }
        });
    }

    /** Gọi BE lấy danh sách và map sang entity ReadingList dùng cho UI */
    public void reload() {
        api.listMine().enqueue(new Callback<List<ReadingListSummaryDTO>>() {
            @Override
            public void onResponse(Call<List<ReadingListSummaryDTO>> call, Response<List<ReadingListSummaryDTO>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    readingLists.postValue(map(res.body()));
                }
            }
            @Override
            public void onFailure(Call<List<ReadingListSummaryDTO>> call, Throwable t) {
                // TODO: log/emit error state
            }
        });
    }

    /** Đổi tên/mô tả; xong thì onDone.run() */
    public void rename(String listId, String newName, String desc, Runnable onDone) {
        api.rename(listId, new UpdateReadingListRequest(newName, desc))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            if (onDone != null) onDone.run();
                        } else {
                            // ở đây có thể phát ra LiveData error hoặc toast tuỳ bạn
                            // ví dụ: 401/403/404/409...
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) { /* TODO: log */ }
                });
    }

    /** Xoá list, xong thì onDone.run() */
    public void deleteList(String listId, Runnable onDone) {
        api.delete(listId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (onDone != null) onDone.run();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { /* TODO: log */ }
        });
    }

    // ===== Mapper DTO -> entity ReadingList (Android side) =====
    private List<ReadingList> map(List<ReadingListSummaryDTO> dtos) {
        List<ReadingList> out = new ArrayList<>();
        if (dtos == null) return out;

        for (ReadingListSummaryDTO d : dtos) {
            ReadingList rl = new ReadingList();
            rl.setId(d.id);
            rl.setName(d.name);
            rl.setDescription(d.description);
            // parse createdAt nếu bạn dùng java.time trên Android 26+
            try {
                LocalDateTime created = LocalDateTime.parse(d.createdAt);
                rl.setCreatedAt(created);
            } catch (Exception ignored) {}
            rl.setPaperCount(d.paperCount);
            out.add(rl);
        }
        return out;
    }

    // (tuỳ chọn) đồng bộ toàn bộ list của current user từ BE -> Room
    public void refreshFromServer() {
        repository.refreshFromServer();
    }
}
