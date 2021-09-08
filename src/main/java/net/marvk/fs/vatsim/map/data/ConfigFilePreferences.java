package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.beans.property.*;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Log4j2
public class ConfigFilePreferences implements Preferences {
    private final Map<String, ObservableValue<?>> observables = new HashMap<>();
    private final Path path;
    private final Adapter<Map<String, ObservableValue<?>>> adapter;

    @Inject
    public ConfigFilePreferences(@Named("userConfigDir") final Path path, @Named("configSerializer") final Adapter<Map<String, ObservableValue<?>>> adapter) {
        this.path = path.resolve("Config.json");
        this.adapter = adapter;
        tryCreateFilterDirectory(path);
        tryLoadConfig();
        booleanProperty("general.debug", false);
        booleanProperty("general.social", true);
        booleanProperty("general.auto_reload", false);
        integerProperty("general.font_size", 12);
        integerProperty("general.map_font_size", 12);
        doubleProperty("general.scroll_speed", 2.25);
        booleanProperty("general.prereleases", false);
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
    public BooleanProperty booleanProperty(final String key) {
        return booleanProperty(key, false);
    }

    @Override
    public BooleanProperty booleanProperty(final String key, final boolean defaultValue) {
        return property(key, () -> new SimpleBooleanProperty(null, key), e -> e.set(defaultValue));
    }

    @Override
    public StringProperty stringProperty(final String key) {
        return stringProperty(key, null);
    }

    @Override
    public StringProperty stringProperty(final String key, final String defaultValue) {
        return property(key, () -> new SimpleStringProperty(null, key), e -> e.set(defaultValue));
    }

    @Override
    public ObjectProperty<Color> colorProperty(final String key) {
        return colorProperty(key, null);
    }

    @Override
    public ObjectProperty<Color> colorProperty(final String key, final Color defaultValue) {
        return property(key, () -> new SimpleObjectProperty<>(null, key), e -> e.set(defaultValue));
    }

    @Override
    public IntegerProperty integerProperty(final String key) {
        return integerProperty(key, 0);
    }

    @Override
    public IntegerProperty integerProperty(final String key, final int defaultValue) {
        return property(key, () -> new SimpleIntegerProperty(null, key), e -> e.set(defaultValue));
    }

    @Override
    public DoubleProperty doubleProperty(final String key) {
        return doubleProperty(key, 0.0);
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
            log.debug(() -> "Changing value of property %s from %s to %s".formatted(key, oldValue, newValue));
            writeConfig();
        });
    }

    @Override
    public Map<String, ObservableValue<?>> values() {
        return Collections.unmodifiableMap(observables);
    }

    private void writeConfig() {
        try {
            log.info("Writing config to %s".formatted(path));
            Files.writeString(path, adapter.serialize(observables), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (final IOException e) {
            log.error("Failed to write config", e);
        }
    }

    public ColorScheme exportColorScheme(final String name) {
        return new ColorScheme(name, exportColorSchemeMap());
    }

    public ColorScheme exportColorScheme(final ColorScheme previous) {
        return new ColorScheme(previous.getUuid(), previous.getName(), exportColorSchemeMap());
    }

    private HashMap<String, Color> exportColorSchemeMap() {
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
    }
}
