package net.marvk.fs.vatsim.map.view.datadetail.detailsubview;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.GeomUtil;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.view.BindingsUtil;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class DetailSubView<DetailViewModel extends DetailSubViewModel<ViewModel>, ViewModel> implements FxmlView<DetailViewModel> {
    @InjectViewModel
    protected DetailViewModel viewModel;

    private List<StringProperty> stringProperties;
    private List<TextArea> textAreas;

    public DetailViewModel getViewModel() {
        return viewModel;
    }

    protected static StringBinding positionLabel(final ObservableObjectValue<Point2D> position) {
        return BindingsUtil.position(position);
    }

    protected static String positionLabel(final Point2D position) {
        return GeomUtil.format(position);
    }

    public void initialize() {
        viewModel.dataProperty().addListener((observable, oldValue, newValue) -> setDataNullable(oldValue, newValue));
    }

    private void setDataNullable(final ViewModel oldValue, final ViewModel newValue) {
        if (newValue == null) {
            clear(oldValue);
        } else {
            setData(newValue);
        }
    }

    protected void clear(final ViewModel oldValue) {
        for (final StringProperty stringProperty : getStringProperties()) {
            stringProperty.unbind();
            stringProperty.set("");
        }
    }

    private List<StringProperty> getStringProperties() {
        if (stringProperties == null) {
            stringProperties = Stream
                    .of(
                            labels().stream().map(Labeled::textProperty),
                            textAreas().stream().map(TextInputControl::textProperty),
                            stringProperties().stream()
                    )
                    .reduce(Stream::concat)
                    .get()
                    .collect(Collectors.toList());
        }

        return stringProperties;
    }

    protected List<TextArea> textAreas() {
        return Collections.emptyList();
    }

    protected List<Label> labels() {
        return Collections.emptyList();
    }

    protected List<StringProperty> stringProperties() {
        return Collections.emptyList();
    }

    protected abstract void setData(final ViewModel data);

    protected static StringBinding doubleToIntString(final ReadOnlyDoubleProperty property, final String suffix) {
        return Bindings.createStringBinding(
                () -> property.intValue() + suffix,
                property
        );
    }

    protected static StringBinding doubleToIntString(final ReadOnlyDoubleProperty property) {
        return doubleToIntString(property, "");
    }

    protected static StringBinding stringToString(final ReadOnlyStringProperty property) {
        return stringToString(property, "");
    }

    protected static StringBinding stringToString(final ReadOnlyStringProperty property, final String suffix) {
        return Bindings.createStringBinding(
                () -> {
                    if (property.get() == null) {
                        return "";
                    }

                    return property.get() + suffix;
                },
                property
        );
    }

    protected static String webColor(final Color color) {
        return color.toString().substring(2);
    }

    protected static String colorKey(final Controller controller) {
        if (controller.getWorkingAirport() != null) {
            return switch (controller.getControllerType()) {
                case ATIS -> "airports.atis_color";
                case DEL -> "airports.delivery_color";
                case GND -> "airports.ground_color";
                case TWR -> "airports.tower_color";
                case APP, DEP -> "airports.approach_circle_color";
                default -> null;
            };
        }

        if (controller.getWorkingFlightInformationRegion() != null) {
            return "active_firs.fir.color";
        }

        if (controller.getWorkingUpperInformationRegion() != null) {
            return "active_uirs.fir.color";
        }

        return null;
    }

    protected static String onlineForString(final ZonedDateTime logonTime) {
        final Duration between = Duration.between(logonTime, ZonedDateTime.now(ZoneId.of("Z")));
        return String.format("%02d:%02d", between.toHoursPart(), between.toMinutesPart());
    }
}
