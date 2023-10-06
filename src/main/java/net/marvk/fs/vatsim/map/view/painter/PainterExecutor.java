package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import lombok.Getter;
import net.marvk.fs.vatsim.map.view.map.PainterMetric;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class PainterExecutor<T> {
    @Getter
    private final Painter<T> painter;
    private final Supplier<Collection<T>> paintablesSupplier;
    @Getter
    private final String name;
    @Getter
    private final String legacyName;

    @Getter
    private long lastDurationNanos = 0L;
    @Getter
    private PainterMetric lastPainterMetric = new PainterMetric();

    private final Predicate<T> filter;

    private PainterExecutor(final String name, final String legacyName, final Painter<T> painter) {
        this(name, legacyName, painter, () -> Collections.singletonList(null), e -> true);
    }

    private PainterExecutor(final String name, final String legacyName, final Painter<T> painter, final Supplier<Collection<T>> paintablesSupplier, final Predicate<T> filter) {
        this.painter = Objects.requireNonNull(painter);
        this.paintablesSupplier = Objects.requireNonNull(paintablesSupplier);
        this.name = Objects.requireNonNullElse(name, legacyName);
        this.legacyName = Objects.requireNonNull(legacyName);
        this.filter = Objects.requireNonNull(filter);
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

    public static <T> PainterExecutor<T> of(final String name, final String legacyName, final Painter<T> painter) {
        return ofCollection(name, legacyName, painter, () -> Collections.singletonList(null));
    }

    public static <T> PainterExecutor<T> ofItem(final String name, final String legacyName, final Painter<T> painter, final Supplier<T> paintablesSupplier, final Predicate<T> filter) {
        return new PainterExecutor<>(name, legacyName, painter, () -> getPaintable(paintablesSupplier), filter);
    }

    public static <T> PainterExecutor<T> ofItem(final String name, final String legacyName, final Painter<T> painter, final Supplier<T> paintablesSupplier) {
        return ofItem(name, legacyName, painter, paintablesSupplier, e -> true);
    }

    public static <T> PainterExecutor<T> ofCollection(final String name, final String legacyName, final Painter<T> painter, final Supplier<Collection<T>> paintablesSupplier, final Predicate<T> filter) {
        return new PainterExecutor<>(name, legacyName, painter, paintablesSupplier, filter);
    }

    public static <T> PainterExecutor<T> ofCollection(final String name, final String legacyName, final Painter<T> painter, final Supplier<Collection<T>> paintablesSupplier) {
        return ofCollection(name, legacyName, painter, paintablesSupplier, e -> true);
    }

    private static <T> List<T> getPaintable(final Supplier<T> paintablesSupplier) {
        final var t = paintablesSupplier.get();
        return t == null ? Collections.emptyList() : Collections.singletonList(t);
    }
}
