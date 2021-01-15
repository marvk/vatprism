package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

import java.util.Map;

public interface Preferences {
    BooleanProperty booleanProperty(final String key);

    BooleanProperty booleanProperty(final String key, final boolean initialValue);

    StringProperty stringProperty(final String key);

    StringProperty stringProperty(final String key, final String defaultValue);

    ObjectProperty<Color> colorProperty(final String key);

    ObjectProperty<Color> colorProperty(final String key, final Color initialValue);

    IntegerProperty integerProperty(final String key);

    IntegerProperty integerProperty(final String key, final int defaultValue);

    DoubleProperty doubleProperty(final String key);

    DoubleProperty doubleProperty(final String key, final double initialValue);

    Map<String, ObservableValue<?>> values();
}
