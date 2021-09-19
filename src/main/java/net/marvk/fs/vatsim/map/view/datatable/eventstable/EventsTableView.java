package net.marvk.fs.vatsim.map.view.datatable.eventstable;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Event;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import net.marvk.fs.vatsim.map.view.datatable.AbstractTableView;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class EventsTableView extends AbstractTableView<EventsTableViewModel, Event> {
    @Inject
    public EventsTableView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
    }

    @Override
    protected void initializeColumns() {
        this.<Event>newColumnBuilder()
            .title("Name")
            .stringObservableValueFactory(Event::nameProperty)
            .sortable()
            .mono(false)
            .widthFactor(2)
            .build();

        this.<ZonedDateTime>newColumnBuilder()
            .title("Date")
            .objectObservableValueFactory(Event::startTimeProperty)
            .toStringMapper(ZonedDateTime::toString)
            .sortable()
            .mono(true)
            .widthFactor(1.5)
            .build();

        this.<ZonedDateTime>newColumnBuilder()
            .title("Local Time")
            .objectObservableValueFactory(Event::startTimeProperty)
            .toStringMapper(this::toLocalDateTimeString)
            .sortable()
            .mono(true)
            .widthFactor(1.5)
            .build();

        this.<Duration>newColumnBuilder()
            .title("Duration")
            .objectObservableValueFactory(Event::durationProperty)
            .toStringMapper(this::durationToString)
            .sortable()
            .mono(true)
            .widthFactor(1.5)
            .build();

        this.<Event>newColumnBuilder()
            .title("Description")
            .stringObservableValueFactory(Event::shortDescriptionProperty)
            .widthFactor(4)
            .build();
    }

    private String toLocalDateTimeString(final ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG));
    }

    private String durationToString(final Duration duration) {
        return "%02d:%02d".formatted(duration.toHours(), duration.toMinutesPart());
    }

}
