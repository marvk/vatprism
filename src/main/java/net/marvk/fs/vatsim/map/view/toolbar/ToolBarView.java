package net.marvk.fs.vatsim.map.view.toolbar;

import com.dlsc.formsfx.model.validators.DoubleRangeValidator;
import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.map.App;
import net.marvk.fs.vatsim.map.view.painter.MetaPainter;
import net.marvk.fs.vatsim.map.view.painter.Painter;
import net.marvk.fs.vatsim.map.view.painter.PainterExecutor;
import net.marvk.fs.vatsim.map.view.painter.Parameter;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ToolBarView implements FxmlView<ToolBarViewModel> {
    @FXML
    private ToggleButton autoReload;
    @InjectViewModel
    private ToolBarViewModel viewModel;

    private final List<Observable> observables = new ArrayList<>();

    @FXML
    private void refresh(final ActionEvent actionEvent) {
        viewModel.refresh();
    }

    public void setAutoReload(final ActionEvent actionEvent) {
        viewModel.setAutoReload(autoReload.isSelected());
    }

    @SneakyThrows
    public void openSettings(final ActionEvent actionEvent) {
        observables.clear();

        final ObservableList<PainterExecutor<?>> executors = viewModel.getPainterExecutors();

        final List<Category> categories = new ArrayList<>();

        for (final PainterExecutor<?> executor : executors) {
            final Setting<?, ?>[] settings = getSettings(executor.getPainter(), executor.getName());
            final Category category = Category.of(executor.getName(), settings);
            categories.add(category);
        }

        final Category painters = Category
                .of("Painters")
                .subCategories(categories.toArray(Category[]::new));

        final PreferencesFx preferencesFx = PreferencesFx.of(
                App.class,
                painters
        );

        preferencesFx.saveSettings(false);
        preferencesFx.show();
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
                settings.addAll(Arrays.asList(getSettings(thePainter, prefix)));
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

        if (Color.class.isAssignableFrom(field.getType())) {
            final Color value = (Color) field.get(painter);
            final ObjectProperty<Color> property = new SimpleObjectProperty<>(value);
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            observables.add(property);
            return Setting.of(name, property).customKey(key(prefix, name));
        }
        if (int.class.isAssignableFrom(field.getType())) {
            final int value = (int) field.get(painter);
            final IntegerProperty property = new SimpleIntegerProperty(value);
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            observables.add(property);
            return Setting.of(name, property)
                          .customKey(key(prefix, name))
                          .validate(IntegerRangeValidator.between((int) min, (int) max, "Not in range"));
        }
        if (double.class.isAssignableFrom(field.getType())) {
            final double value = (double) field.get(painter);
            final DoubleProperty property = new SimpleDoubleProperty(value);
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            observables.add(property);
            return Setting.of(name, property)
                          .customKey(key(prefix, name))
                          .validate(DoubleRangeValidator.between(min, max, "Not in range"));
        }
        if (boolean.class.isAssignableFrom(field.getType())) {
            final boolean value = (boolean) field.get(painter);
            final BooleanProperty property = new SimpleBooleanProperty(value);
            property.addListener((observable, oldValue, newValue) -> setField(field, painter, newValue));
            observables.add(property);
            return Setting.of(name, property)
                          .customKey(key(prefix, name));
        }
        return null;
    }

    @SneakyThrows
    private void setField(final Field field, final Painter<?> painter, final Object newValue) {
        field.set(painter, newValue);
        viewModel.triggerRepaint();
    }

    private static String key(final String... keys) {
        final String s = Arrays
                .stream(keys)
                .map(e -> e.toLowerCase(Locale.ROOT))
                .map(e -> e.replaceAll("^[A-Za-z0-9.]", ""))
                .map(e -> e.replaceAll("\\s", "_"))
                .collect(Collectors.joining("."));
        return s + s.hashCode();
    }
}
