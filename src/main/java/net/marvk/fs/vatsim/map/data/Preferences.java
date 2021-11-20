package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

import java.util.Map;

public interface Preferences {
    default BooleanProperty booleanProperty(final String key) {
        return booleanProperty(key, false);
    }

    BooleanProperty booleanProperty(final String key, final boolean initialValue);

    default StringProperty stringProperty(final String key) {
        return stringProperty(key, null);
    }

    StringProperty stringProperty(final String key, final String defaultValue);

    default ObjectProperty<Color> colorProperty(final String key) {
        return colorProperty(key, null);
    }

    ObjectProperty<Color> colorProperty(final String key, final Color initialValue);

    default IntegerProperty integerProperty(final String key) {
        return integerProperty(key, 0);
    }

    IntegerProperty integerProperty(final String key, final int defaultValue);

    default DoubleProperty doubleProperty(final String key) {
        return doubleProperty(key, 0.0);
    }

    DoubleProperty doubleProperty(final String key, final double initialValue);

    Map<String, ObservableValue<?>> values();
}
