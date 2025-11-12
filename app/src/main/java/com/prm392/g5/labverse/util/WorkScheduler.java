package com.prm392.g5.labverse.util;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.prm392.g5.labverse.worker.SyncIncomingWorker;
import com.prm392.g5.labverse.worker.SyncOutgoingWorker;
import com.prm392.g5.labverse.worker.PrefetchRecentWorker;

public class WorkScheduler {

    public static void initSync(Context context) {
        Constraints NET = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        WorkManager wm = WorkManager.getInstance(context);

        // Đồng bộ dữ liệu mới từ BE (papers, reading lists, annotations)
        wm.enqueueUniqueWork(
                "sync_incoming",
                ExistingWorkPolicy.REPLACE,
                new OneTimeWorkRequest.Builder(SyncIncomingWorker.class)
                        .setConstraints(NET)
                        .build()
        );

        // Gửi các thay đổi offline (annotation DIRTY) lên BE
        wm.enqueueUniqueWork(
                "sync_outgoing",
                ExistingWorkPolicy.KEEP,
                new OneTimeWorkRequest.Builder(SyncOutgoingWorker.class)
                        .setConstraints(NET)
                        .build()
        );

        // Prefetch PDFs gần đây (metadata + file)
        wm.enqueueUniqueWork(
                "prefetch_recent",
                ExistingWorkPolicy.REPLACE,
                new OneTimeWorkRequest.Builder(PrefetchRecentWorker.class)
                        .setConstraints(NET)
                        .build()
        );
    }
}
