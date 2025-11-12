package com.prm392.g5.labverse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prm392.g5.labverse.apiService.PaperApiService;
import com.prm392.g5.labverse.apiService.ReadingListApi;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dto.paper.PaperSummaryDTO;
import com.prm392.g5.labverse.dto.readingList.AddExistingPaperRequest;
import com.prm392.g5.labverse.dto.readingList.ReadingListItemDTO;
import com.prm392.g5.labverse.dto.readingList.ReadingListResponse;
import com.prm392.g5.labverse.dto.readingList.UpdateReadingListRequest;
import com.prm392.g5.labverse.entity.Paper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadingListDetailViewModel extends AndroidViewModel {

    private final ReadingListApi readingListApi;
    private final PaperApiService paperApi;

    private String currentListId;

    // Papers trong list (DTO từ server)
    private final MutableLiveData<List<PaperSummaryDTO>> papersInListDto = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<PaperSummaryDTO>> getPapersInReadingListDto() { return papersInListDto; }

    // Tất cả papers (server DTO) cho màn chọn
    private final MutableLiveData<List<PaperSummaryDTO>> allPapersDto = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<PaperSummaryDTO>> getAllPapersFromServer() { return allPapersDto; }

    private final MutableLiveData<List<Paper>> papersInListUi =
            new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Paper>> getPapersInReadingList() { return papersInListUi; }

    public ReadingListDetailViewModel(@NonNull Application app) {
        super(app);
        readingListApi = RetrofitClient.getInstance().create(ReadingListApi.class);
        paperApi = RetrofitClient.getInstance().create(PaperApiService.class);
    }

    // ====== READING LIST DETAIL ======
    public void setReadingListId(@NonNull String listId) {
        this.currentListId = listId;
        reloadPapersInReadingList();
    }

    public void load(String listId) {
        readingListApi.getPapers(listId).enqueue(new Callback<List<PaperSummaryDTO>>() {
            @Override public void onResponse(Call<List<PaperSummaryDTO>> call, Response<List<PaperSummaryDTO>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    papersInListDto.postValue(res.body());
                    List<PaperSummaryDTO> dto = res.body();
                    papersInListUi.postValue(mapDtoToPaper(dto));
                }
            }
            @Override public void onFailure(Call<List<PaperSummaryDTO>> call, Throwable t) { /* TODO: log */ }
        });
    }

    public void reloadPapersInReadingList() {
        if (currentListId == null || currentListId.isEmpty()) return;
        load(currentListId);
    }

    public void rename(String listId, String newName, @Nullable String desc, Runnable onDone){
        UpdateReadingListRequest body = new UpdateReadingListRequest();
        body.name = newName;
        body.description = desc;
        readingListApi.rename(listId, body).enqueue(new SimpleCb<>(r -> onDone.run()));
    }

    public void renameReadingList(String listId, String newName) {
        rename(listId, newName, null, this::reloadPapersInReadingList);
    }

    public void deleteList(String listId, Runnable onDone){
        readingListApi.delete(listId).enqueue(new SimpleCb<>(r -> onDone.run()));
    }

    /** Thêm paper có sẵn vào list (append nếu position = null) */
    public void addExisting(String listId, String paperId, @Nullable Integer position, Runnable onDone){
        AddExistingPaperRequest body = new AddExistingPaperRequest();
        body.paperId = paperId;
        body.position = position;
        readingListApi.addExisting(listId, body).enqueue(new SimpleCb<>(r -> { load(listId); onDone.run(); }));
    }

    /** POST /api/reading-lists/{id}/papers với body { paperId, position } */
    public void addExistingPaper(String listId,
                                 String paperId,
                                 @Nullable Runnable onDone,
                                 @Nullable Consumer<String> onError) {
        if (listId == null || listId.isEmpty()) listId = currentListId;

        AddExistingPaperRequest body = new AddExistingPaperRequest();
        body.paperId = paperId;
        body.position = null; // append

        readingListApi.addExisting(listId, body).enqueue(new Callback<ReadingListItemDTO>() {
            @Override public void onResponse(Call<ReadingListItemDTO> call, Response<ReadingListItemDTO> response) {
                if (response.isSuccessful()) {
                    reloadPapersInReadingList();
                    if (onDone != null) onDone.run();
                    return;
                }
                if (response.code() == 409) {
                    if (onError != null) onError.accept("Paper is already in this list");
                } else if (response.code() == 401) {
                    if (onError != null) onError.accept("Unauthorized — please re-login");
                } else {
                    if (onError != null) onError.accept("HTTP " + response.code());
                }
            }
            @Override public void onFailure(Call<ReadingListItemDTO> call, Throwable t) {
                if (onError != null) onError.accept(t.getMessage());
            }
        });
    }

    /** DELETE /api/reading-lists/{id}/papers/{paperId} */
    public void deletePaperFromReadingList(String listId, String paperId,
                                           @Nullable Runnable onDone,
                                           @Nullable Consumer<String> onError) {
        if (listId == null || listId.isEmpty()) listId = currentListId;
        readingListApi.deletePaper(listId, paperId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> res) {
                if (res.isSuccessful()) {
                    reloadPapersInReadingList();
                    if (onDone != null) onDone.run();
                } else {
                    if (onError != null) onError.accept("HTTP " + res.code());
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                if (onError != null) onError.accept(t.getMessage());
            }
        });
    }

    // ====== ALL PAPERS (màn chọn) ======
    /** Gọi BE: GET /api/papers/all (lấy theo principal trong JWT) */
    public void fetchAllPapersForPicker() {
        paperApi.getAllPapersForCurrentUser().enqueue(new Callback<List<PaperSummaryDTO>>() {
            @Override public void onResponse(Call<List<PaperSummaryDTO>> call, Response<List<PaperSummaryDTO>> res) {
                if (res.isSuccessful() && res.body()!=null) {
                    allPapersDto.postValue(res.body());
                }
            }
            @Override public void onFailure(Call<List<PaperSummaryDTO>> call, Throwable t) { /* TODO: log */ }
        });
    }

    // ====== Helper ======
    private static class SimpleCb<T> implements Callback<T> {
        private final Consumer<Response<T>> ok;
        SimpleCb(Consumer<Response<T>> ok){ this.ok=ok; }
        @Override public void onResponse(Call<T> c, Response<T> r){ if(r.isSuccessful()) ok.accept(r); }
        @Override public void onFailure(Call<T> c, Throwable t){ /* log */ }
    }

    /** Nếu adapter vẫn cần entity Paper cho UI, map nhanh từ DTO: */
    public static List<Paper> mapDtoToPaper(List<PaperSummaryDTO> dtos) {
        List<Paper> out = new ArrayList<>();
        if (dtos == null) return out;
        for (PaperSummaryDTO d : dtos) {
            Paper p = new Paper();
            p.setId(d.id);
            p.setTitle(d.title);
            p.setAuthorName(d.authorName);
            p.setJournalName(d.journalName);
            p.setTotalPage(d.totalPage);
            // p.setCurrentPage(d.progress); // nếu muốn hiển thị tiến độ
            out.add(p);
        }
        return out;
    }
}
