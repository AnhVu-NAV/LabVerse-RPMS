package com.prm392.g5.labverse.di;

import android.content.Context;
import com.prm392.g5.labverse.apiService.SyncApiService;
import com.prm392.g5.labverse.apiService.PaperApiService;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.repository.SyncRepository;

public class DI {
    public static PaperApiService api() {
        return RetrofitClient.getInstance().create(PaperApiService.class);
    }

    public static SyncApiService syncApi() {
        return RetrofitClient.getInstance().create(SyncApiService.class);
    }

    public static SyncRepository repo(Context ctx, String userId) {
        return new SyncRepository(ctx, syncApi(), userId);
    }
}
