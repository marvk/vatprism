package net.marvk.fs.vatsim.map;

import java.time.Duration;
import java.util.concurrent.*;

public class Debouncer {
    private final Callable<?> callable;
    private final Duration delay;
    private final ScheduledExecutorService executor;

    private ScheduledFuture<?> future;

    public Debouncer(final Runnable runnable, final Duration delay) {
        this((Callable<Void>) () -> {
            runnable.run();
            return null;
        }, delay);
    }

    public Debouncer(final Callable<?> callable, final Duration delay) {
        this.callable = callable;
        this.delay = delay;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void callDebounced() {
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }

        future = executor.schedule(callable, delay.toMillis(), TimeUnit.MILLISECONDS);
    }
}