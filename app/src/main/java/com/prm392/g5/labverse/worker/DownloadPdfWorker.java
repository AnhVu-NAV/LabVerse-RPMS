//package com.prm392.g5.labverse.worker;
//
//import android.content.Context;
//import androidx.annotation.NonNull;
//import androidx.work.Worker;
//import androidx.work.WorkerParameters;
//
//import com.prm392.g5.labverse.config.AppDatabase;
//import com.prm392.g5.labverse.di.DI;
//import com.prm392.g5.labverse.dto.S3SignedUrlResponse;
//
//import okhttp3.Request;
//import okhttp3.ResponseBody;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//
//public class DownloadPdfWorker extends Worker {
//    public DownloadPdfWorker(@NonNull Context ctx, @NonNull WorkerParameters p){ super(ctx,p); }
//
//    @NonNull @Override public Result doWork() {
//        String paperId = getInputData().getString("paperId");
//        if (paperId == null) return Result.failure();
//        try {
//            S3SignedUrlResponse s = DI.api().getPdfSignedUrl(paperId).execute().body();
//            if (s == null || s.getUrl() == null) return Result.retry();
//
//            File out = new File(getApplicationContext().getFilesDir(), "papers/" + paperId + ".pdf");
//            if (!out.getParentFile().exists()) out.getParentFile().mkdirs();
//
//            ResponseBody body = DI.okhttp().newCall(new Request.Builder().url(s.getUrl()).build())
//                    .execute().body();
//            if (body == null) return Result.retry();
//
//            try (InputStream in = body.byteStream(); FileOutputStream fos = new FileOutputStream(out)) {
//                byte[] buf = new byte[8192]; int r;
//                while ((r = in.read(buf)) != -1) fos.write(buf, 0, r);
//            }
//
//            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
//            db.paperDashboardDao().updateLocalPath(paperId, out.getAbsolutePath());
//
//            return Result.success();
//        } catch (Exception e) {
//            return Result.retry();
//        }
//    }
//}
