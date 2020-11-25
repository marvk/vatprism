package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.Canvas;

import java.util.Collection;
import java.util.stream.Stream;

public interface Painter<T> {
    void paint(final Canvas canvas, final T t);

    @SuppressWarnings("unchecked")
    default void paint(final Canvas canvas, final T... paintables) {
        for (final T t : paintables) {
            paint(canvas, t);
        }
    }

    default void paint(final Canvas canvas, final Collection<T> paintables) {
        for (final T t : paintables) {
            paint(canvas, t);
        }
    }

    default void paint(final Canvas canvas, final Stream<T> paintables) {
        paintables.forEach(t -> paint(canvas, t));
    }
}
