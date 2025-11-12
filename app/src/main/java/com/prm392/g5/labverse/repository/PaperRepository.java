package com.prm392.g5.labverse.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;
import java.time.*;
import java.time.format.DateTimeFormatter;

import com.prm392.g5.labverse.apiService.AuthApiService;
import com.prm392.g5.labverse.apiService.PaperApiService;
import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dao.PaperDashboardDao;
import com.prm392.g5.labverse.dto.S3SignedUrlResponse;
import com.prm392.g5.labverse.dto.paper.AddPaperRequest;
import com.prm392.g5.labverse.dto.paper.AddPaperResponse;
import com.prm392.g5.labverse.dto.paper.PaperInfoResponse;
import com.prm392.g5.labverse.dto.paper.PageResponse;
import com.prm392.g5.labverse.dto.paper.PaperCardDto;
import com.prm392.g5.labverse.entity.PaperCache;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaperRepository {

    private PaperApiService paperApiService;
    private PaperDashboardDao dashDao;

    public PaperRepository() {
        paperApiService = RetrofitClient.getInstance().create(PaperApiService.class);
    }

    public void getUploadUrl(String s3Key, Callback<S3SignedUrlResponse> callback){
        paperApiService.getUploadUrl(s3Key).enqueue(callback);
    }

    public void addPaper(AddPaperRequest request, Callback<AddPaperResponse> callback){
        paperApiService.addPaper(request).enqueue(callback);
    }

    public void getPaperInfo(String id, Callback<PaperInfoResponse> callback){
        paperApiService.getPaperInfo(id).enqueue(callback);
    }

    public void getDownloadUrl(String s3Key, Callback<S3SignedUrlResponse> callback){
        paperApiService.getDownloadUrl(s3Key).enqueue(callback);
    }

    public PaperRepository(Context ctx) {
        paperApiService = RetrofitClient.getInstance().create(PaperApiService.class);
        AppDatabase db = Room.databaseBuilder(ctx, AppDatabase.class, "LabVerse.db")
                .fallbackToDestructiveMigration()
                .build();
        dashDao = db.paperDashboardDao();
    }


    public LiveData<List<PaperCache>> observe(String filter) {
        switch (filter) {
            case "recently_read": return dashDao.recentlyRead();
            case "favorites":     return dashDao.favorites();
            case "recently_added":
            default:              return dashDao.recentlyAdded();
        }
    }

    public void sync(String userId, String filter) {
        paperApiService.listPapers(userId, filter, 0, 50).enqueue(new Callback<PageResponse<PaperCardDto>>() {
            @Override public void onResponse(Call<PageResponse<PaperCardDto>> call, Response<PageResponse<PaperCardDto>> resp) {
                if (!resp.isSuccessful() || resp.body()==null) return;
                List<PaperCache> list = new ArrayList<>();
                for (PaperCardDto d : resp.body().content) list.add(map(d));
                Executors.newSingleThreadExecutor().execute(() -> dashDao.upsertAll(list));
            }
            @Override public void onFailure(Call<PageResponse<PaperCardDto>> call, Throwable t) {}
        });
    }

    private PaperCache map(PaperCardDto d) {
        PaperCache e = new PaperCache();
        e.id = d.id; e.title = d.title; e.authors = d.authors; e.journal = d.journal;
        e.status = d.status; e.progress = d.progress; e.favorite = d.favorite;
        e.createdAtEpoch = toEpoch(d.createdAt);
        e.lastReadAtEpoch = d.lastReadAt == null ? null : toEpoch(d.lastReadAt);
        return e;
    }

    private static long toEpoch(String iso) {
        if (iso == null || iso.isEmpty()) return 0L;
        try {
            // Nếu backend có Z/+offset thì dùng Instant.parse luôn
            if (iso.endsWith("Z") || iso.contains("+")) {
                return Instant.parse(iso).toEpochMilli();
            }
            // Mặc định: chuỗi không có zone -> parse LocalDateTime
            LocalDateTime ldt = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            // Chọn zone hiển thị/logic của app (thường là systemDefault hoặc UTC)
            return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            android.util.Log.w("PaperRepository", "Bad date: " + iso, e);
            return 0L;
        }
    }
}
