package net.marvk.fs.vatsim.map.view.painter;

import net.marvk.fs.vatsim.map.view.map.PainterMetric;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CompositeMapPainter<T> extends DisableablePainter<T> {
    private Collection<? extends Painter<?>> painters = null;

    protected abstract Collection<? extends Painter<?>> painters();

    protected Collection<? extends Painter<?>> getPainters() {
        if (painters == null) {
            painters = painters();
        }

        return painters;
    }

    @Override
    public PainterMetric getMetricsSnapshot() {
        final List<PainterMetric> metrics = getPainters()
                .stream()
                .map(Painter::getMetricsSnapshot)
                .collect(Collectors.toList());

        return PainterMetric.ofMetrics(metrics);
    }

    @Override
    public void beforeAllRender() {
        for (final Painter<?> painter : getPainters()) {
            painter.beforeAllRender();
        }
    }

    @Override
    public void beforeEachRender() {
        for (final Painter<?> painter : getPainters()) {
            painter.afterEachRender();
        }
    }

    @Override
    public void afterEachRender() {
        for (final Painter<?> painter : getPainters()) {
            painter.afterEachRender();
        }
    }

    @Override
    public void afterAllRender() {
        for (final Painter<?> painter : getPainters()) {
            painter.afterAllRender();
        }
    }
}
