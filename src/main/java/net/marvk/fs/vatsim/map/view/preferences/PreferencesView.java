package net.marvk.fs.vatsim.map.view.preferences;

import com.dlsc.formsfx.model.validators.DoubleRangeValidator;
import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.saxsys.mvvmfx.FluentViewLoader;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.map.App;
import net.marvk.fs.vatsim.map.data.Preferences;
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
import java.util.stream.Collectors;

@Singleton
public class PreferencesView {
    private static final String INFO_STYLE = "-fx-text-fill: #aaaaaa; -fx-font-size: 10;";
    private static final String WARNING_STYLE = "-fx-text-fill: darkred; -fx-font-weight: bold; -fx-font-size: 10;";
    private final Preferences preferences;
    private final SettingsScope settingsScope;
    private PreferencesFx preferencesFx;

    @Inject
    public PreferencesView(final Preferences preferences, final SettingsScope settingsScope) {
        this.preferences = preferences;
        this.settingsScope = settingsScope;
        this.settingsScope.getPainters()
                          .addListener((ListChangeListener<PainterExecutor<?>>) c -> getPreferencesDialog());

    }

    public void show() {
        getPreferencesDialog().show(true);
    }

    private PreferencesFx getPreferencesDialog() {
        if (preferencesFx == null) {
            preferencesFx = createPreferencesDialog();
        }

        return preferencesFx;
    }

    @SneakyThrows
    private PreferencesFx createPreferencesDialog() {
        return PreferencesFx.of(
                App.class,
                general(),
                colorSchemes(),
                ui(),
                painters()
        ).saveSettings(false);
    }

    private Category ui() {
        final BooleanProperty autoColor = preferences.booleanProperty("ui.auto_color");
        final BooleanProperty autoShade = preferences.booleanProperty("ui.auto_shade");
        final ObjectProperty<Color> backgroundColor = preferences.colorProperty("ui.background_base_color");
        final BooleanProperty backgroundShadingInverted = preferences.booleanProperty("ui.invert_background_shading");
        final ObjectProperty<Color> textColor = preferences.colorProperty("ui.text_base_color");
        final BooleanProperty textShadingInverted = preferences.booleanProperty("ui.invert_text_shading");

        return Category.of("User Interface",
                Setting.of("World Color As Base", autoColor),
                Setting.of(infoLabel("Enable if the color if ui colors should by synced with the color of the World Fill", INFO_STYLE)),
                Setting.of("Background Color Base", backgroundColor),
                Setting.of("Text Color Base", textColor),
                Setting.of("Shade Automatically", autoShade),
                Setting.of(infoLabel("Enable if VATprism should attempt to automatically detect the best shading options", INFO_STYLE)),
                Setting.of("Invert Background Color Shading", backgroundShadingInverted),
                Setting.of("Invert Text Color Shading", textShadingInverted)
        );
    }

    private Label infoLabel(final String s, final String style) {
        final Label result = new Label(s);
        result.setPadding(new Insets(0, 0, 0, 20));
        result.setStyle(style);
        return result;
    }

    private Category colorSchemes() {
        final Parent view = FluentViewLoader.javaView(ColorSchemeView.class).load().getView();
        return Category.of("Color Schemes", Setting.of(view));
    }

    private Category general() {
        final BooleanProperty social = preferences.booleanProperty("general.social");
        final IntegerProperty uiFontSize = preferences.integerProperty("general.font_size");
        final IntegerProperty property = preferences.integerProperty("general.map_font_size");
        final DoubleProperty scrollSpeed = preferences.doubleProperty("general.scroll_speed");

        final BooleanProperty debug = preferences.booleanProperty("general.debug");
        final BooleanProperty prereleases = preferences.booleanProperty("general.prereleases");
        final BooleanProperty deleteOldLogs = preferences.booleanProperty("general.delete_old_logs");

        debug.addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                preferences.booleanProperty("metrics.enabled").set(false);
            }
        });
        debug.set(true);

        final IntegerProperty uiScale = preferences.integerProperty("general.ui_scale");
        uiScale.bind(uiFontSize.divide(12.0));

        return Category.of(
                "General",
                Group.of(
                        Setting.of("Show Twitch Stream Links", social),
                        Setting.of("UI Font Size", uiFontSize, 4, 72),
                        Setting.of("Map Font Size", property, 4, 72),
                        Setting.of("Scroll Speed", scrollSpeed, 1.1, 16, 2)
                ),
                Group.of("Advanced",
                        Setting.of("Enable Debug Mode", debug),
                        Setting.of(infoLabel("Enables the debug monitor button and possibly other debug information", INFO_STYLE)),
                        Setting.of("Prerelease Updates", prereleases),
                        Setting.of(infoLabel("Be warned: Prerelease updates are not stable, anything might break at any time.", WARNING_STYLE)),
                        Setting.of("Prune old logs", deleteOldLogs),
                        Setting.of(infoLabel("Automatically delete logs older than 14 days at startup", INFO_STYLE))
                )
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
//                    observables.put(o.getName(), o);
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
            final ObjectProperty<Color> property = preferences.colorProperty(key, (Color) field.get(painter));
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            setField(field, painter, property.getValue());
            if (bind) {
                property.bind(preferences.colorProperty(bindToKey));
            }
            if (visible) {
                return Setting.of(name, property).customKey(key);
            }
        } else if (int.class.isAssignableFrom(field.getType())) {
            final IntegerProperty property = preferences.integerProperty(key, (int) field.get(painter));
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            setField(field, painter, property.getValue());
            if (bind) {
                property.bind(preferences.integerProperty(bindToKey));
            }
            if (visible) {
                return Setting.of(name, property)
                              .customKey(key)
                              .validate(IntegerRangeValidator.between((int) min, (int) max, "Not in range"));
            }
        } else if (double.class.isAssignableFrom(field.getType())) {
            final DoubleProperty property = preferences.doubleProperty(key, (double) field.get(painter));
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            setField(field, painter, property.getValue());
            if (bind) {
                property.bind(preferences.doubleProperty(bindToKey));
            }
            if (visible) {
                return Setting.of(name, property)
                              .customKey(key)
                              .validate(DoubleRangeValidator.between(min, max, "Not in range"));
            }
        } else if (boolean.class.isAssignableFrom(field.getType())) {
            final BooleanProperty property = preferences.booleanProperty(key, (boolean) field.get(painter));
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            setField(field, painter, property.getValue());
            if (bind) {
                property.bind(preferences.booleanProperty(bindToKey));
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
        return Arrays
                .stream(keys)
                .map(e -> e.toLowerCase(Locale.ROOT))
                .map(e -> e.replaceAll("\\s", "_"))
                .map(e -> e.replaceAll("[^A-Za-z0-9._]", ""))
                .collect(Collectors.joining("."));
    }
}
