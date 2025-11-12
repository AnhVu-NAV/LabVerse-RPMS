package com.prm392.g5.labverse.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tiện ích cho xử lý nền (database/network)
 */
public class AppExecutors {
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();

    public static ExecutorService io() {
        return IO_EXECUTOR;
    }
}
