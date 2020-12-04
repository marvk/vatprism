package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class PainterExecutor<T> {
    private final Painter<T> painter;
    private final Supplier<Collection<T>> paintablesSupplier;
    private final String name;

    private long lastDurationNanos = 0L;

    private PainterExecutor(final String name, final Painter<T> painter) {
        this(name, painter, () -> Collections.singletonList(null));
    }

    private PainterExecutor(final String name, final Painter<T> painter, final Supplier<Collection<T>> paintablesSupplier) {
        this.painter = painter;
        this.paintablesSupplier = paintablesSupplier;
        this.name = name;
    }

    public void paint(final GraphicsContext c) {
        final long start = System.nanoTime();
        painter.beforeAllRender();
        for (final T t : paintablesSupplier.get()) {
            painter.beforeEachRender();
            painter.paint(c, t);
            painter.afterEachRender();
        }
        painter.afterAllRender();
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

    public static <T> PainterExecutor<T> of(final String name, final Painter<T> painter) {
        return ofCollection(name, painter, () -> Collections.singletonList(null));
    }

    public static <T> PainterExecutor<T> ofItem(final String name, final Painter<T> painter, final Supplier<T> paintablesSupplier) {
        return new PainterExecutor<>(name, painter, () -> getPaintable(paintablesSupplier));
    }

    private static <T> List<T> getPaintable(final Supplier<T> paintablesSupplier) {
        final var t = paintablesSupplier.get();
        return t == null ? Collections.emptyList() : Collections.singletonList(t);
    }

    public static <T> PainterExecutor<T> ofCollection(final String name, final Painter<T> painter, final Supplier<Collection<T>> paintablesSupplier) {
        return new PainterExecutor<>(name, painter, paintablesSupplier);
    }
}
