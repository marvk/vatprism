package net.marvk.fs.vatsim.map.view.datatable.clientstable;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.data.ImmutableObjectProperty;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import net.marvk.fs.vatsim.map.view.datatable.AbstractTableView;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public abstract class AbstractClientsTableView<ViewModel extends SimpleTableViewModel<Data>, Data extends Client> extends AbstractTableView<ViewModel, Data> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("E HH:mmVV");

    @Inject
    public AbstractClientsTableView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
    }

    @Override
    protected void initializeColumns() {
        this.<Number>newColumnBuilder()
                .title("CID")
                .objectObservableValueFactory(Client::cidProperty)
                .toStringMapper(Object::toString)
                .sortable()
                .mono(true)
                .build();

        this.<String>newColumnBuilder()
                .title("Callsign")
                .stringObservableValueFactory(Client::callsignProperty)
                .sortable()
                .mono(true)
                .build();

        this.<String>newColumnBuilder()
                .title("Name")
                .stringObservableValueFactory(Client::realNameProperty)
                .sortable()
                .build();

        this.<ZonedDateTime>newColumnBuilder()
                .title("Online since")
                .objectObservableValueFactory(Client::logonTimeProperty)
                .toStringMapper(FORMATTER::format)
                .sortable()
                .mono(true)
                .build();

        this.<Duration>newColumnBuilder()
                .title("Online for")
                .objectObservableValueFactory(e -> new ImmutableObjectProperty<>(Duration.between(e.getLogonTime(), ZonedDateTime
                        .now(ZoneId.of("Z")))))
                .toStringMapper(AbstractClientsTableView::formatDuration)
                .sortable()
                .mono(true)
                .build();
    }

    private static String formatDuration(final Duration duration) {
        return pad(duration.toHoursPart()) + ":" + pad(duration.toMinutesPart());
    }

    private static String pad(final int i) {
        return pad(String.valueOf(i));
    }

    private static String pad(final String s) {
        return "0".repeat(Math.max(0, 2 - s.length())) + s;
    }
}
