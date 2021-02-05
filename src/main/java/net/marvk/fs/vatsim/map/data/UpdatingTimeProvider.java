package net.marvk.fs.vatsim.map.data;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class UpdatingTimeProvider implements TimeProvider {
    private final ReadOnlyObjectWrapper<ZonedDateTime> currentTime = new ReadOnlyObjectWrapper<>(currentTime());
    private final Duration period;
    private CurrentTimeService timeService;

    public UpdatingTimeProvider(final Duration period) {
        this(period, false);
    }

    public UpdatingTimeProvider(final Duration period, final boolean startAutomatically) {
        this.period = period;

        if (startAutomatically) {
            start();
        }
    }

    public void start() {
        if (isStarted()) {
            throw new IllegalStateException("Service has already been started");
        }

        timeService = new CurrentTimeService();
        timeService.setOnCancelled(e -> this.timeService = null);
        timeService.setPeriod(javafx.util.Duration.seconds(period.getSeconds()));
        timeService.start();
    }

    public void stop() {
        if (isStarted()) {
            timeService.cancel();
        }
    }

    public boolean isStarted() {
        return timeService != null;
    }

    @Override
    public ZonedDateTime getCurrentTime() {
        return currentTime.get();
    }

    @Override
    public ReadOnlyObjectProperty<ZonedDateTime> currentTimeProperty() {
        return currentTime.getReadOnlyProperty();
    }

    private class CurrentTimeService extends ScheduledService<Void> {
        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() {
                    Platform.runLater(() -> currentTime.set(currentTime()));
                    return null;
                }
            };
        }
    }

    private static ZonedDateTime currentTime() {
        return ZonedDateTime.now(ZoneId.of("Z"));
    }
}
