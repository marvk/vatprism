package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import net.marvk.fs.vatsim.map.view.map.PainterMetric;

import java.util.Collection;
import java.util.stream.Stream;

public interface Painter<T> {
    void paint(final GraphicsContext context, final T t);

    @SuppressWarnings("unchecked")
    default void paint(final GraphicsContext context, final T... paintables) {
        for (final T t : paintables) {
            paint(context, t);
        }
    }

    default void paint(final GraphicsContext context, final Collection<T> paintables) {
        for (final T t : paintables) {
            paint(context, t);
        }
    }

    default void paint(final GraphicsContext context, final Stream<T> paintables) {
        paintables.forEach(t -> paint(context, t));
    }

    default void beforeAllRender() {
    }

    default void beforeEachRender() {
    }

    default void afterEachRender() {
    }

    default void afterAllRender() {
    }

    boolean isEnabled();

    PainterMetric getMetricsSnapshot();
}
