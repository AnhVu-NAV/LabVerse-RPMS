package com.prm392.g5.labverse.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;

import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.entity.PaperCache;
import com.prm392.g5.labverse.session.Session;

import java.util.List;

public class PrefetchRecentWorker extends Worker {
    public PrefetchRecentWorker(@NonNull Context ctx, @NonNull WorkerParameters p){ super(ctx,p); }

    @NonNull @Override public Result doWork() {
        try {
            String userId = Session.getUserId(getApplicationContext());
            if (userId == null) return Result.failure();

            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<PaperCache> recent = db.paperDashboardDao().getRecentCacheNow(userId, 20);

            for (PaperCache p : recent) {
                if (!fileExists(p.localPath)) {
                    db.paperDashboardDao().markPendingDownload(p.id);
                }
            }
            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }

    private boolean fileExists(String path) {
        if (path == null || path.isEmpty()) return false;
        try { return new java.io.File(path).exists(); } catch (Exception ignore) { return false; }
    }
}
