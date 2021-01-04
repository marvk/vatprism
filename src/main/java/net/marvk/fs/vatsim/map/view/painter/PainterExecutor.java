package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import net.marvk.fs.vatsim.map.view.map.PainterMetric;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class PainterExecutor<T> {
    private final Painter<T> painter;
    private final Supplier<Collection<T>> paintablesSupplier;
    private final String name;
    private final Predicate<T> filter;

    private long lastDurationNanos = 0L;
    private PainterMetric lastPainterMetric = new PainterMetric();

    private PainterExecutor(final String name, final Painter<T> painter) {
        this(name, painter, () -> Collections.singletonList(null), e -> true);
    }

    private PainterExecutor(final String name, final Painter<T> painter, final Supplier<Collection<T>> paintablesSupplier, final Predicate<T> filter) {
        this.painter = painter;
        this.paintablesSupplier = paintablesSupplier;
        this.name = name;
        this.filter = filter;
    }

    public void paint(final GraphicsContext c) {
        final long start = System.nanoTime();
        painter.beforeAllRender();
        if (painter.isEnabled()) {
            for (final T t : paintablesSupplier.get()) {
                if (filter.test(t)) {
                    painter.beforeEachRender();
                    painter.paint(c, t);
                    painter.afterEachRender();
                }
            }
        }
        painter.afterAllRender();
        lastPainterMetric = painter.getMetricsSnapshot();
        lastDurationNanos = System.nanoTime() - start;
    }

    public String getName() {
        return name;
    }

    public Painter<T> getPainter() {
        return painter;
    }

    public long getLastDurationNanos() {
        return lastDurationNanos;
    }

    public PainterMetric getLastPainterMetric() {
        return lastPainterMetric;
    }

    public static <T> PainterExecutor<T> of(final String name, final Painter<T> painter) {
        return ofCollection(name, painter, () -> Collections.singletonList(null));
    }

    public static <T> PainterExecutor<T> ofItem(final String name, final Painter<T> painter, final Supplier<T> paintablesSupplier, final Predicate<T> filter) {
        return new PainterExecutor<>(name, painter, () -> getPaintable(paintablesSupplier), filter);
    }

    public static <T> PainterExecutor<T> ofItem(final String name, final Painter<T> painter, final Supplier<T> paintablesSupplier) {
        return ofItem(name, painter, paintablesSupplier, e -> true);
    }

    public static <T> PainterExecutor<T> ofCollection(final String name, final Painter<T> painter, final Supplier<Collection<T>> paintablesSupplier, final Predicate<T> filter) {
        return new PainterExecutor<>(name, painter, paintablesSupplier, filter);
    }

    public static <T> PainterExecutor<T> ofCollection(final String name, final Painter<T> painter, final Supplier<Collection<T>> paintablesSupplier) {
        return ofCollection(name, painter, paintablesSupplier, e -> true);
    }

    private static <T> List<T> getPaintable(final Supplier<T> paintablesSupplier) {
        final var t = paintablesSupplier.get();
        return t == null ? Collections.emptyList() : Collections.singletonList(t);
    }
}
