package net.marvk.fs.vatsim.map.view.preferences;

import com.dlsc.formsfx.model.validators.DoubleRangeValidator;
import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
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
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

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
        getPreferencesDialog().show(true);
    }

    public BooleanProperty booleanProperty(final String key) {
        return property(key, () -> new SimpleBooleanProperty(null, key));
    }

    public BooleanProperty booleanProperty(final String key, final boolean initialValue) {
        final BooleanProperty booleanProperty = booleanProperty(key);
        booleanProperty.set(initialValue);
        return booleanProperty;
    }

    public StringProperty stringProperty(final String key) {
        return property(key, () -> new SimpleStringProperty(null, key));
    }

    public StringProperty stringProperty(final String key, final String defaultValue) {
        final StringProperty stringProperty = stringProperty(key);
        stringProperty.set(defaultValue);
        return stringProperty;
    }

    public ObjectProperty<Color> colorProperty(final String key) {
        return property(key, () -> new SimpleObjectProperty<>(null, key));
    }

    public ObjectProperty<Color> colorProperty(final String key, final Color initialValue) {
        final ObjectProperty<Color> colorProperty = colorProperty(key);
        colorProperty.set(initialValue);
        return colorProperty;
    }

    public IntegerProperty integerProperty(final String key) {
        return property(key, () -> new SimpleIntegerProperty(null, key));
    }

    public IntegerProperty integerProperty(final String key, final int defaultValue) {
        final IntegerProperty integerProperty = integerProperty(key);
        integerProperty.set(defaultValue);
        return integerProperty;
    }

    public DoubleProperty doubleProperty(final String key) {
        return property(key, () -> new SimpleDoubleProperty(null, key));
    }

    public DoubleProperty doubleProperty(final String key, final double initialValue) {
        final DoubleProperty doubleProperty = doubleProperty(key);
        doubleProperty.set(initialValue);
        return doubleProperty;
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
        return PreferencesFx.of(App.class, general(), style(), painters())
                            .saveSettings(false);
    }

    private Category style() {
        return Category.of("Style");
    }

    private Category general() {
        final BooleanProperty debug = booleanProperty("general.debug");
        debug.addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                booleanProperty("metrics.show").set(false);
            }
        });
        debug.set(true);

        return Category.of(
                "General",
                FontIcon.of(Octicons.GEAR_16),
                Setting.of("Enable Debug Mode", debug),
                Setting.of("UI Font Size", integerProperty("general.font_size", 12), 4, 72),
                Setting.of("Map Font Size", integerProperty("general.map_font_size", 12), 4, 72),
                Setting.of("Scroll Speed", doubleProperty("general.scroll_speed", 2.25), 1.1, 16, 2)
        );
    }

    private Category painters() throws IllegalAccessException {
        final Category[] painters = paintersCategories(settingsScope.getPainters());

        return Category
                .of("Painters", FontIcon.of(Octicons.PAINTBRUSH_16))
                .subCategories(painters)
                .expand();
    }

    private Category[] paintersCategories(final ObservableList<PainterExecutor<?>> executors) throws IllegalAccessException {
        final List<Category> categories = new ArrayList<>();

        for (final PainterExecutor<?> executor : executors) {
            final Group[] groups = getSettings(executor.getPainter(), executor.getName());
            for (final Group group : groups) {
                for (final Setting<?, ?> setting : group.getSettings()) {
                    final Property<?> o = setting.valueProperty();
                    observables.put(o.getName(), o);
                }
            }
            final Category category = Category.of(executor.getName(), groups);
            categories.add(category);
        }
        return categories.toArray(Category[]::new);
    }

    private Group[] getSettings(final Painter<?> painter, final String prefix) throws IllegalAccessException {

        final List<Field> fields = Arrays
                .stream(fields(painter))
                .filter(e -> e.isAnnotationPresent(net.marvk.fs.vatsim.map.view.painter.Group.class) || e.isAnnotationPresent(Parameter.class) || e
                        .isAnnotationPresent(MetaPainter.class))
                .peek(e -> e.setAccessible(true))
                .collect(Collectors.toList());

        final ArrayList<Group> result = new ArrayList<>();

        String currentGroup = prefix;
        final Collection<Setting<?, ?>> settings = new ArrayList<>();

        for (final Field field : fields) {
            if (field.isAnnotationPresent(net.marvk.fs.vatsim.map.view.painter.Group.class)) {
                if (!settings.isEmpty()) {
                    settings.removeIf(Objects::isNull);

                    if (!settings.isEmpty()) {
                        result.add(Group.of(currentGroup, settings.toArray(Setting[]::new)));
                    }
                    settings.clear();
                }

                final var groupAnnotation = field.getAnnotation(net.marvk.fs.vatsim.map.view.painter.Group.class);
                currentGroup = groupAnnotation.value();
            }

            if (field.isAnnotationPresent(Parameter.class)) {
                settings.add(extracted(painter, field, prefix));
            }
        }

        if (!settings.isEmpty()) {
            settings.removeIf(Objects::isNull);

            if (!settings.isEmpty()) {
                result.add(Group.of(currentGroup, settings.toArray(Setting[]::new)));
            }
        }

        for (final Field field : fields) {
            if (field.isAnnotationPresent(MetaPainter.class)) {
                final MetaPainter metaPainter = field.getAnnotation(MetaPainter.class);

                final Painter<?> thePainter = (Painter<?>) field.get(painter);
                result.addAll(Arrays.asList(getSettings(thePainter, prefix + "." + metaPainter.value())));
            }
        }

        return result.toArray(Group[]::new);
    }

    private static Field[] fields(final Painter<?> painter) {
        Class<?> clazz = painter.getClass();

        final ArrayList<Field> result = new ArrayList<>();

        while (clazz != Object.class) {
            final Field[] declaredFields = clazz.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                result.add(i, declaredFields[i]);
            }
            clazz = clazz.getSuperclass();
        }

        return result.toArray(Field[]::new);
    }

    private Setting<?, ?> extracted(final Painter<?> painter, final Field field, final String prefix) throws IllegalAccessException {
        final Parameter parameter = field.getAnnotation(Parameter.class);

        final String name = parameter.value();

        final double min = parameter.min();
        final double max = parameter.max();

        final String bindToKey = parameter.bind();
        final boolean bind = !bindToKey.isBlank();
        final boolean visible = parameter.visible();
        final String key = key(prefix, name);
        if (Color.class.isAssignableFrom(field.getType())) {
            final ObjectProperty<Color> property = colorProperty(key);
            property.set((Color) field.get(painter));
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            if (bind) {
                property.bind(colorProperty(bindToKey));
            }
            if (visible) {
                return Setting.of(name, property).customKey(key);
            }
        } else if (int.class.isAssignableFrom(field.getType())) {
            final IntegerProperty property = integerProperty(key);
            property.set((int) field.get(painter));
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            if (bind) {
                property.bind(integerProperty(bindToKey));
            }
            if (visible) {
                return Setting.of(name, property)
                              .customKey(key)
                              .validate(IntegerRangeValidator.between((int) min, (int) max, "Not in range"));
            }
        } else if (double.class.isAssignableFrom(field.getType())) {
            final DoubleProperty property = doubleProperty(key);
            property.set((double) field.get(painter));
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            if (bind) {
                property.bind(doubleProperty(bindToKey));
            }
            if (visible) {
                return Setting.of(name, property)
                              .customKey(key)
                              .validate(DoubleRangeValidator.between(min, max, "Not in range"));
            }
        } else if (boolean.class.isAssignableFrom(field.getType())) {
            final BooleanProperty property = booleanProperty(key);
            property.setValue((boolean) field.get(painter));
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            if (bind) {
                property.bind(booleanProperty(bindToKey));
            }
            if (visible) {
                return Setting.of(name, property)
                              .customKey(key);
            }
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
