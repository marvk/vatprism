package net.marvk.fs.vatsim.map.view.painter;

import net.marvk.fs.vatsim.map.view.map.MapVariables;
import net.marvk.fs.vatsim.map.view.map.PainterMetric;

import java.util.Objects;

public abstract class AlwaysOnMapPainter<T> implements Painter<T> {
    protected final MapVariables mapVariables;
    protected final PainterHelper painterHelper;

    public AlwaysOnMapPainter(final MapVariables mapVariables) {
        this.mapVariables = Objects.requireNonNull(mapVariables);
        this.painterHelper = new PainterHelper(mapVariables);
    }

    @Override
    public PainterMetric createMetricsSnapshot() {
        return painterHelper.metricSnapshot();
    }
}

