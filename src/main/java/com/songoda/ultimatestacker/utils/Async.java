package com.songoda.ultimatestacker.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Async {

    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void run(Runnable runnable) {
        executor.execute(runnable);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}
