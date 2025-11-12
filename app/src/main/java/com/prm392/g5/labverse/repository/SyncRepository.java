package com.prm392.g5.labverse.repository;

import android.content.Context;

import com.prm392.g5.labverse.apiService.SyncApiService;
import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.dto.sync.DeltaResponse;
import com.prm392.g5.labverse.dto.sync.PushResult;
import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.entity.PaperAnnotation;
import com.prm392.g5.labverse.entity.SyncMeta;
import com.prm392.g5.labverse.filter.PaperFilter;

import java.util.List;

public class SyncRepository {
    private final AppDatabase db;
    private final SyncApiService api; // retrofit của bạn
    private final String userId;

    public SyncRepository(Context ctx, SyncApiService api, String userId) {
        this.db = AppDatabase.getInstance(ctx);
        this.api = api;
        this.userId = userId;
    }

    public List<Paper> searchLocal(String keyword, PaperFilter f) {
        String kw = toLike(keyword);
        return db.paperDao().searchAdvancedOwned(
                userId, kw, toLike(f.author), toLike(f.journal), toLike(f.tag), toLike(f.year)
        );
    }

    public void pullDelta() throws Exception {
        String since = db.syncMetaDao().getValue(userId, "lastDeltaSyncAt");
        DeltaResponse delta = api.fetchDelta(userId, since).execute().body();
        db.runInTransaction(() -> {
            db.paperDao().upsertAll(delta.papersMappedForRoom(userId));
            db.readingListDao().upsertAll(delta.listsMappedForRoom(userId));
            db.paperAnnotationDao().upsertAll(delta.annotationsMappedForRoom(userId));
            db.syncMetaDao().upsert(new SyncMeta(userId, "lastDeltaSyncAt", nowIso()));
        });
    }

    public void pushDirty() throws Exception {
        List<PaperAnnotation> dirty = db.paperAnnotationDao().getDirtyNow(userId);
        if (dirty.isEmpty()) return;
        PushResult res = api.pushAnnotations(userId, dirty).execute().body();
        db.runInTransaction(() -> {
            for (PaperAnnotation a : res.toCleanEntities()) {
                a.setSyncState("CLEAN");
            }
            db.paperAnnotationDao().upsertAll(res.toCleanEntities());
        });
    }

    private String toLike(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        t = t.replace("\\", "\\\\").replace("%","\\%").replace("_","\\_");
        return "%" + t + "%";
    }

    private String nowIso() {
        return java.time.OffsetDateTime.now().toString();
    }
}
