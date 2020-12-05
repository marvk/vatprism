package net.marvk.fs.vatsim.map.view.painter;

import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.Collection;

public abstract class CompositeMapPainter<T> extends MapPainter<T> {
    private Collection<Painter<?>> painters = null;

    public CompositeMapPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    protected abstract Collection<Painter<?>> painters();

    private Collection<Painter<?>> getPainters() {
        if (painters == null) {
            painters = painters();
        }

        return painters;
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
