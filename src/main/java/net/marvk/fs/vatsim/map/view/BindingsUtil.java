package net.marvk.fs.vatsim.map.view;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.map.GeomUtil;

public final class BindingsUtil {
    private BindingsUtil() {
        throw new AssertionError("No instances of utility class " + BindingsUtil.class);
    }

    public static StringBinding position(final ObservableObjectValue<Point2D> position) {
        return Bindings.createStringBinding(
                () -> GeomUtil.format(position.get()),
                position
        );
    }
}
