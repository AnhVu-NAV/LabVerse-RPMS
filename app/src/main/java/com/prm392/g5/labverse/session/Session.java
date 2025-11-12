package com.prm392.g5.labverse.session;

import android.content.Context;
import com.prm392.g5.labverse.config.SharePreferenceManager;

public final class Session {
    private Session() {}
    public static String getUserId(Context ctx) {
        return SharePreferenceManager.getInstance().getUserId();
    }
}
