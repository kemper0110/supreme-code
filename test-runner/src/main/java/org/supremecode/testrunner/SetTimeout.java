package org.supremecode.testrunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SetTimeout {
    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    CompletableFuture<Void> setTimeout(int timeout) {
        final var timeoutCF = new CompletableFuture<Void>();
        scheduler.schedule(() -> timeoutCF.complete(null), timeout, TimeUnit.MILLISECONDS);
        return timeoutCF;
    }
}
