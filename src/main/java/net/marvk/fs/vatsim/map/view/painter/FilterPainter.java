package net.marvk.fs.vatsim.map.view.painter;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import net.marvk.fs.vatsim.map.data.Filter;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FilterPainter extends CompositeMapPainter<Pilot> {

    private final List<FilteredPilotPainter> filterPainters;
    private MapVariables mapVariables;

    public FilterPainter(final MapVariables mapVariables, final ObservableList<Filter> filters) {
        this.mapVariables = mapVariables;
        filterPainters = filters
                .stream()
                .map(FilteredPilotPainter::new)
                .collect(Collectors.toCollection(ArrayList::new));

        filters.addListener((ListChangeListener<Filter>) c -> {
            while (c.next()) {
                filterPainters.removeIf(e -> c.getRemoved().contains(e.filter));
                c.getAddedSubList().forEach(e -> filterPainters.add(new FilteredPilotPainter(e)));
            }
        });
    }

    @Override
    protected Collection<? extends Painter<?>> painters() {
        return filterPainters;
    }

    @Override
    protected Collection<? extends Painter<?>> getPainters() {
        return filterPainters;
    }

    @Override
    public void paint(final GraphicsContext context, final Pilot pilot) {
        for (final FilteredPilotPainter painter : filterPainters) {
            painter.paint(context, pilot);
        }
    }

    private class FilteredPilotPainter extends MapPainter<Pilot> {
        private final PilotPainter painter;
        private final Filter filter;

        public FilteredPilotPainter(final Filter filter) {
            super(FilterPainter.this.mapVariables);

            this.painter = new PilotPainter(mapVariables, filter.getTextColor(), filter.getBackgroundColor());
            this.filter = filter;
        }

        @Override
        public void paint(final GraphicsContext c, final Pilot pilot) {
            if (filter.test(pilot)) {
                painter.paint(c, pilot);
            }
        }
    }
}
