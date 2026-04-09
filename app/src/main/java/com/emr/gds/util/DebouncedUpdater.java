package com.emr.gds.util;

import javafx.application.Platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DebouncedUpdater {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pending;
    
    public void schedule(Runnable task, long delayMs) {
        if (pending != null) {
            pending.cancel(false);
        }
        pending = executor.schedule(() -> Platform.runLater(task), delayMs, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
