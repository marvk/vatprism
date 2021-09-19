package net.marvk.fs.vatsim.map.view.events;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import net.marvk.fs.vatsim.map.data.Event;
import net.marvk.fs.vatsim.map.data.EventRepository;

import java.time.LocalDateTime;

public class EventsViewModel implements ViewModel {
    private final EventRepository eventRepository;

    private final ReadOnlyObjectWrapper<LocalDateTime> now = new ReadOnlyObjectWrapper<>(LocalDateTime.now());

    @Inject
    public EventsViewModel(final EventRepository eventRepository) {
        this.eventRepository = eventRepository;
        startTimeService();
    }

    private void startTimeService() {
        final ScheduledService<LocalDateTime> scheduledService = new ScheduledService<>() {
            @Override
            protected Task<LocalDateTime> createTask() {
                return new Task<>() {
                    @Override
                    protected LocalDateTime call() {
                        return LocalDateTime.now();
                    }
                };
            }
        };

        scheduledService.setPeriod(Duration.seconds(1));
        scheduledService.start();
        scheduledService.setOnSucceeded(
                event -> Platform.runLater(() -> now.set((LocalDateTime) event.getSource().getValue()))
        );
    }

    public ObservableList<Event> events() {
        return eventRepository.list();
    }

    public LocalDateTime getNow() {
        return now.get();
    }

    public ReadOnlyObjectProperty<LocalDateTime> nowProperty() {
        return now.getReadOnlyProperty();
    }
}
