package net.marvk.fs.vatsim.map.view.preferences;

import com.dlsc.formsfx.model.validators.DoubleRangeValidator;
import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.map.App;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.SettingsScope;
import net.marvk.fs.vatsim.map.view.painter.MetaPainter;
import net.marvk.fs.vatsim.map.view.painter.Painter;
import net.marvk.fs.vatsim.map.view.painter.PainterExecutor;
import net.marvk.fs.vatsim.map.view.painter.Parameter;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Singleton
public class Preferences {
    private final Map<String, Observable> observables = new HashMap<>();
    private final SettingsScope settingsScope;

    private PreferencesFx preferencesFx;

    @Inject
    public Preferences(final SettingsScope settingsScope) {
        this.settingsScope = settingsScope;

        this.settingsScope.getPainters()
                          .addListener((ListChangeListener<PainterExecutor<?>>) c -> getPreferencesDialog());
    }

    public void show() {
        getPreferencesDialog().show();
    }

    public BooleanProperty booleanProperty(final String key) {
        return property(key, () -> new SimpleBooleanProperty(null, key));
    }

    public StringProperty stringProperty(final String key) {
        return property(key, () -> new SimpleStringProperty(null, key));
    }

    public ObjectProperty<Color> colorProperty(final String key) {
        return property(key, () -> new SimpleObjectProperty<>(null, key));
    }

    public IntegerProperty integerProperty(final String key) {
        return property(key, () -> new SimpleIntegerProperty(null, key));
    }

    public DoubleProperty doubleProperty(final String key) {
        return property(key, () -> new SimpleDoubleProperty(null, key));
    }

    @SuppressWarnings("unchecked")
    private <T extends Observable> T property(final String key, final Supplier<T> defaultSupplier) {
        observables.computeIfAbsent(key, e -> defaultSupplier.get());
        return (T) observables.get(key);
    }

    private PreferencesFx getPreferencesDialog() {
        if (preferencesFx == null) {
            preferencesFx = createPreferencesDialog();
        }

        return preferencesFx;
    }

    @SneakyThrows
    private PreferencesFx createPreferencesDialog() {
        final ObservableList<PainterExecutor<?>> executors = settingsScope.getPainters();

        final List<Category> categories = new ArrayList<>();

        for (final PainterExecutor<?> executor : executors) {
            final Setting<?, ?>[] settings = getSettings(executor.getPainter(), executor.getName());
            for (final Setting<?, ?> setting : settings) {
                final Property<?> o = setting.valueProperty();
                observables.put(o.getName(), o);
            }
            final Category category = Category.of(executor.getName(), settings);
            categories.add(category);
        }

        final Category painters = Category
                .of("Painters")
                .subCategories(categories.toArray(Category[]::new))
                .expand();

        return PreferencesFx.of(App.class, painters)
                            .saveSettings(false);
    }

    private Setting<?, ?>[] getSettings(final Painter<?> painter, final String prefix) throws IllegalAccessException {
        final Collection<Setting<?, ?>> settings = new ArrayList<>();

        final List<Field> fields = Arrays
                .stream(painter.getClass().getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(Parameter.class) || e.isAnnotationPresent(MetaPainter.class))
                .peek(e -> e.setAccessible(true))
                .collect(Collectors.toList());

        for (final Field field : fields) {
            if (field.isAnnotationPresent(MetaPainter.class)) {
                final MetaPainter metaPainter = field.getAnnotation(MetaPainter.class);

                final Painter<?> thePainter = (Painter<?>) field.get(painter);
                settings.addAll(Arrays.asList(getSettings(thePainter, metaPainter.value() + prefix)));
            } else {
                settings.add(extracted(painter, field, prefix));
            }
        }

        settings.removeIf(Objects::isNull);

        return settings.toArray(Setting[]::new);
    }

    private Setting<?, ?> extracted(final Painter<?> painter, final Field field, final String prefix) throws IllegalAccessException {
        final Parameter parameter = field.getAnnotation(Parameter.class);

        final String name = parameter.value();

        final double min = parameter.min();
        final double max = parameter.max();

        final String key = key(prefix, name);
        if (Color.class.isAssignableFrom(field.getType())) {
            final ObjectProperty<Color> property = colorProperty(key);
            property.set((Color) field.get(painter));
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            return Setting.of(name, property).customKey(key);
        }
        if (int.class.isAssignableFrom(field.getType())) {
            final IntegerProperty property = integerProperty(key);
            property.set((int) field.get(painter));
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            return Setting.of(name, property)
                          .customKey(key)
                          .validate(IntegerRangeValidator.between((int) min, (int) max, "Not in range"));
        }
        if (double.class.isAssignableFrom(field.getType())) {
            final DoubleProperty property = doubleProperty(key);
            property.set((double) field.get(painter));
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            return Setting.of(name, property)
                          .customKey(key)
                          .validate(DoubleRangeValidator.between(min, max, "Not in range"));
        }
        if (boolean.class.isAssignableFrom(field.getType())) {
            final BooleanProperty property = booleanProperty(key);
            property.setValue((boolean) field.get(painter));
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            return Setting.of(name, property)
                          .customKey(key);
        }
        return null;
    }

    @SneakyThrows
    private static void setField(final Field field, final Painter<?> painter, final Object newValue) {
        field.set(painter, newValue);
        Notifications.REPAINT.publish();
    }

    private static String key(final String... keys) {
        final String s = Arrays
                .stream(keys)
                .map(e -> e.toLowerCase(Locale.ROOT))
                .map(e -> e.replaceAll("\\s", "_"))
                .map(e -> e.replaceAll("[^A-Za-z0-9._]", ""))
                .collect(Collectors.joining("."));
        return s;
    }
}
