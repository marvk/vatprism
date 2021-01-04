package net.marvk.fs.vatsim.map.view.painter;

import net.marvk.fs.vatsim.map.view.map.MapVariables;
import net.marvk.fs.vatsim.map.view.map.PainterMetric;

import java.util.Objects;

public abstract class MapPainter<T> extends DisableablePainter<T> {
    protected final MapVariables mapVariables;
    protected final PainterHelper painterHelper;

    public MapPainter(final MapVariables mapVariables) {
        this.mapVariables = Objects.requireNonNull(mapVariables);
        this.painterHelper = new PainterHelper(mapVariables);
    }

    @Override
    public PainterMetric getMetricsSnapshot() {
        return painterHelper.metricSnapshot();
    }
}
