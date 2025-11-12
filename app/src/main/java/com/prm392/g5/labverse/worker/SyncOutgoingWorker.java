package com.prm392.g5.labverse.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.prm392.g5.labverse.di.DI;
import com.prm392.g5.labverse.repository.SyncRepository;
import com.prm392.g5.labverse.session.Session;

public class SyncOutgoingWorker extends Worker {
    public SyncOutgoingWorker(@NonNull Context ctx, @NonNull WorkerParameters p){ super(ctx,p); }

    @NonNull @Override public Result doWork() {
        try {
            String userId = Session.getUserId(getApplicationContext());
            if (userId == null) return Result.failure();
            SyncRepository repo = DI.repo(getApplicationContext(), userId);
            repo.pushDirty();
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
