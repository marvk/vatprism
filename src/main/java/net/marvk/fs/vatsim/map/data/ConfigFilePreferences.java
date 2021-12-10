package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.beans.property.*;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.Debouncer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class ConfigFilePreferences implements Preferences, Writable {
    private final Map<String, ObservableValue<?>> observables = new HashMap<>();
    private final Path path;
    private final Adapter<Map<String, ObservableValue<?>>> adapter;

    private final Debouncer writeDebouncer = new Debouncer(this::write, Duration.ofSeconds(1));

    @Inject
    public ConfigFilePreferences(@Named("userConfigDir") final Path path, @Named("configSerializer") final Adapter<Map<String, ObservableValue<?>>> adapter) {
        this.path = path.resolve("Config.json");
        this.adapter = adapter;
        tryCreateFilterDirectory(path);
        tryLoadConfig();

        migrateOldProperties();

        booleanProperty("window.rememberLastPosition", false);
        booleanProperty("window.nextStartupIsValid", false);
        booleanProperty("window.fullscreen");
        booleanProperty("window.maximized");
        doubleProperty("window.x");
        doubleProperty("window.y");
        doubleProperty("window.width");
        doubleProperty("window.height");

        booleanProperty("general.debug", false);
        booleanProperty("general.use_map_data_cache", true);
        integerProperty("general.map_data_cache_ttl", (int) Duration.ofDays(7).toSeconds());
        booleanProperty("general.social", true);
        booleanProperty("general.auto_reload", false);
        integerProperty("general.font_size", 12);
        integerProperty("general.map_font_size", 12);
        doubleProperty("general.scroll_speed", 2.25);
        booleanProperty("general.prereleases", false);
        booleanProperty("general.delete_old_logs", true);
        stringProperty("meta.version", "0.0.0");

        booleanProperty("ui.auto_color", true);
        booleanProperty("ui.auto_shade", true);
        colorProperty("ui.background_base_color", Color.valueOf("1a130a"));
        booleanProperty("ui.invert_background_shading", false);
        booleanProperty("ui.reverse_background_shading", false);
        colorProperty("ui.text_base_color", Color.valueOf("1a130a"));
        booleanProperty("ui.invert_text_shading", false);

        booleanProperty("context_menu.show_all_firs", false);
        booleanProperty("context_menu.show_all_uirs", false);
        booleanProperty("context_menu.show_all_airports", false);
        booleanProperty("context_menu.show_all_pilots", false);
    }

    private void migrateOldProperties() {
        List.of("airports", "search_items.airport", "selected_item.airport").forEach(prefix -> {
            this.<Boolean>migrateProperty(
                    "%s.paint_labels_of_uncontrolled_airports_with_destinations_or_arrivals".formatted(prefix),
                    "%s.paint_labels_of_uncontrolled_airports_with_arrivals_or_departures".formatted(prefix),
                    this::booleanProperty
            );
            this.<Boolean>migrateProperty(
                    "%s.paint_uncontrolled_airports_with_destinations_or_arrivals".formatted(prefix),
                    "%s.paint_uncontrolled_airports_with_arrivals_or_departures".formatted(prefix),
                    this::booleanProperty
            );
        });

        deletePropertiesWithPrefix("search_items.airports");
        deletePropertiesWithPrefix("selected_item.airports");
        writeDebounced();
    }

    private void deleteProperties(final String... keys) {
        for (final String key : keys) {
            deleteProperty(key);
        }
    }

    private void deletePropertiesWithPrefix(final String prefix) {
        final List<String> keysToDelete = observables
                .keySet()
                .stream()
                .filter(e -> e.startsWith(prefix))
                // Collect to avoid ConcurrentModificationException
                .collect(Collectors.toList());

        if (keysToDelete.isEmpty()) {
            log.debug("Skipped deletion of keys with prefix %s because no keys with this prefix exist".formatted(prefix));
        } else {
            keysToDelete.forEach(this::deleteProperty);
            log.info("Deleted %d keys with prefix %s".formatted(keysToDelete.size(), prefix));
        }
    }

    private void deleteProperty(final String key) {
        final ObservableValue<?> oldProperty = observables.get(key);
        if (oldProperty != null) {
            log.info("Deleted old property %s".formatted(key));
            observables.remove(key);
        } else {
            log.debug("Skipped deletion of old property %s because it already didn't exist");
        }
    }

    private <T> void migrateProperty(final String oldKey, final String newKey, final BiFunction<String, T, ObservableValue<T>> newObservableValueSupplier) {
        final ObservableValue<T> oldProperty = (ObservableValue<T>) observables.get(oldKey);
        if (oldProperty != null) {
            observables.remove(oldKey);
            if (observables.containsKey(newKey)) {
                log.info("Deleted old property %s during migration, new property %s already existed".formatted(oldKey, newKey));
            } else {
                final T oldValue = (T) oldProperty.getValue();
                newObservableValueSupplier.apply(newKey, oldValue);
                log.info("Migrated old property %s to new property %s".formatted(oldKey, newKey));
            }
        } else {
            log.debug("Skipped migration of old property %s to new property %s because old property did not exist".formatted(oldKey, newKey));
        }
    }

    private void tryLoadConfig() {
        try {
            log.info("Loading config from %s".formatted(path));
            final String raw = Files.readString(path);
            log.debug("Parsing config \n%s".formatted(raw));
            final Map<String, ObservableValue<?>> deserialize = adapter.deserialize(raw);
            final var entries = deserialize
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());

            log.debug("Loading config entries");
            for (final var e : entries) {
                log.debug("Loading config entry (%s, %s)".formatted(e.getKey(), e.getValue().getValue()));
                observables.put(e.getKey(), e.getValue());
                registerListener(e.getKey(), e.getValue());
            }
        } catch (final IOException e) {
            log.error("Failed to load config", e);
        }
    }

    private Path tryCreateFilterDirectory(final Path path) {
        try {
            log.info(("Creating config directory %s").formatted(path));
            return Files.createDirectories(path);
        } catch (final IOException e) {
            log.error("Unable to create config directory, no user data will be saved", e);
            return null;
        }
    }

    @Override
    public BooleanProperty booleanProperty(final String key, final boolean defaultValue) {
        return property(key, () -> new SimpleBooleanProperty(null, key), e -> e.set(defaultValue));
    }

    @Override
    public StringProperty stringProperty(final String key, final String defaultValue) {
        return property(key, () -> new SimpleStringProperty(null, key), e -> e.set(defaultValue));
    }

    @Override
    public ObjectProperty<Color> colorProperty(final String key, final Color defaultValue) {
        return property(key, () -> new SimpleObjectProperty<>(null, key), e -> e.set(defaultValue));
    }

    @Override
    public IntegerProperty integerProperty(final String key, final int defaultValue) {
        return property(key, () -> new SimpleIntegerProperty(null, key), e -> e.set(defaultValue));
    }

    @Override
    public DoubleProperty doubleProperty(final String key, final double defaultValue) {
        return property(key, () -> new SimpleDoubleProperty(null, key), e -> e.set(defaultValue));
    }

    @SuppressWarnings("unchecked")
    private <T extends ObservableValue<E>, E> T property(final String key, final Supplier<T> defaultSupplier, final Consumer<T> ifPresent) {
        // TODO clean this mess
        observables.computeIfPresent(key, (k, v) -> {
            if (v.getValue() == null) {
                ifPresent.accept((T) v);
            }
            return v;
        });
        observables.computeIfAbsent(key, e -> {
            final T result = createProperty(key, defaultSupplier);
            ifPresent.accept(result);
            return result;
        });
        return (T) observables.get(key);
    }

    private <T extends ObservableValue<E>, E> T createProperty(final String key, final Supplier<T> propertySupplier) {
        final T result = propertySupplier.get();
        registerListener(key, result);
        return result;
    }

    private <T extends ObservableValue<E>, E> void registerListener(final String key, final T observableValue) {
        observableValue.addListener((observable, oldValue, newValue) -> {
            log.info(() -> "Changing value of property %s from %s to %s".formatted(key, oldValue, newValue));
            writeDebounced();
        });
    }

    @Override
    public Map<String, ObservableValue<?>> values() {
        return Collections.unmodifiableMap(observables);
    }

    private void writeDebounced() {
        log.debug("Debounce write config");
        writeDebouncer.callDebounced();
    }

    public ColorScheme exportColorScheme(final String name) {
        return new ColorScheme(name, exportColorSchemeColorMap(), exportColorSchemeToggleMap());
    }

    public ColorScheme exportColorScheme(final ColorScheme previous) {
        return new ColorScheme(previous.getUuid(), ColorScheme.CURRENT_VERSION, previous.getName(), exportColorSchemeColorMap(), exportColorSchemeToggleMap());
    }

    private Map<String, Boolean> exportColorSchemeToggleMap() {
        return Stream.of("ui.auto_color", "ui.auto_shade", "ui.reverse_background_shading", "ui.invert_background_shading", "ui.invert_text_shading")
                     .collect(Collectors.toMap(Function.identity(), e -> booleanProperty(e).get(), (a, b) -> b, HashMap::new));
    }

    private HashMap<String, Color> exportColorSchemeColorMap() {
        final HashMap<String, Color> result = new HashMap<>();

        for (final Map.Entry<String, ObservableValue<?>> e : observables.entrySet()) {
            final ObservableValue<?> observable = e.getValue();
            if (!(observable instanceof ObservableStringValue) && (observable instanceof ObservableObjectValue)) {
                result.put(e.getKey(), (Color) observable.getValue());
            }
        }

        return result;
    }

    public void importColorScheme(final ColorScheme colorScheme) {
        for (final Map.Entry<String, Color> e : colorScheme.getColorMap().entrySet()) {
            colorProperty(e.getKey()).set(e.getValue());
        }

        for (final Map.Entry<String, Boolean> e : colorScheme.getToggleMap().entrySet()) {
            booleanProperty(e.getKey()).set(e.getValue());
        }
    }

    @Override
    public void write() {
        try {
            log.info("Writing config to %s".formatted(path));
            Files.writeString(path, adapter.serialize(observables), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (final IOException e) {
            log.error("Failed to write config", e);
        }
    }
}
