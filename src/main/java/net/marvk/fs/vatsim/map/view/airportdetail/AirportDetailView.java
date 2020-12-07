package net.marvk.fs.vatsim.map.view.airportdetail;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.data.ControllerType;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.view.BindingsUtil;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailSubView;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AirportDetailView extends DataDetailSubView<AirportDetailViewModel, Airport> {
    private static final String HYPERLINK_LABEL = "hyperlink-label";

    @FXML
    private HBox noControllers;
    @FXML
    private VBox controllersContainer;

    @FXML
    private GridPane controllersGrid;
    @FXML
    private Label icao;
    @FXML
    private Label fir;
    @FXML
    private Label position;
    @FXML
    private Label name;

    @Override
    protected List<TextArea> textAreas() {
        return Collections.emptyList();
    }

    @Override
    protected List<Label> labels() {
        return List.of(position, name, icao);
    }

    @Override
    protected void setData(final Airport airport) {
        name.setText(airport.getNames().get(0));
        position.textProperty().bind(BindingsUtil.position(airport.positionProperty()));
        icao.setText(airport.getIcao());
        final FlightInformationRegionBoundary firb = airport.getFlightInformationRegionBoundary();

        if (firb == null) {
            fir.getStyleClass().remove(HYPERLINK_LABEL);
            fir.setText(null);
        } else {
            fir.getStyleClass().add(HYPERLINK_LABEL);
            fir.setText(firb.getIcao());
        }

        airport.getControllers().addListener(this::controllersChanged);
        controllersChanged(null);
    }

    private void setControllerPanes(final Boolean newValue) {
        final ObservableList<Node> children = controllersContainer.getChildren();
        children.setAll(children.get(0));

        if (newValue) {
            children.add(controllersGrid);
        } else {
            children.add(noControllers);
        }
    }

    private void controllersChanged(final ListChangeListener.Change<? extends Controller> change) {
        controllersGrid.getChildren().clear();
        final List<Controller> controllers = viewModel
                .getData()
                .getControllers()
                .stream()
                .sorted((o1, o2) -> ControllerType.COMPARATOR.compare(o1.getControllerType(), o2.getControllerType()))
                .collect(Collectors.toList());

        setControllerPanes(!controllers.isEmpty());

        for (int i = 0; i < controllers.size(); i++) {
            final Controller controller = controllers.get(i);

            final String typeLabel = controller.getControllerType().toString();
            final Label type = new Label(" ".repeat(4 - typeLabel.length()) + typeLabel);
            type.setPadding(new Insets(0, 2, 0, 2));
            type.setStyle("-fx-text-fill: #" + web(color("type_label_color")));
            type.getStyleClass().add("mono");
            final Pane typeHolder = new Pane(type);
            typeHolder.setStyle("-fx-background-color: #" + web(color(key(controller.getControllerType()))));
            GridPane.setConstraints(typeHolder, 0, i);
            controllersGrid.getChildren().add(typeHolder);
            GridPane.setHgrow(typeHolder, Priority.NEVER);

            final Label frequency = new Label(controller.getFrequency());
            frequency.getStyleClass().add("mono");
            GridPane.setConstraints(frequency, 1, i);
            controllersGrid.getChildren().add(frequency);
            GridPane.setHgrow(frequency, Priority.NEVER);

            final Label callsign = new Label(controller.getCallsign());
            callsign.getStyleClass().add("mono");
            callsign.getStyleClass().add("hyperlink-label");
            GridPane.setConstraints(callsign, 2, i);
            controllersGrid.getChildren().add(callsign);
            callsign.setOnMouseClicked(e -> viewModel.setDataDetail(controller));
            GridPane.setHgrow(callsign, Priority.ALWAYS);
        }
    }

    private String web(final Color color) {
        return color.toString().substring(2);
    }

    private Color color(final String key) {
        if (key == null) {
            return Color.TRANSPARENT;
        }

        return viewModel.getPreferences().colorProperty("airports." + key).get();
    }

    private static String key(final ControllerType controllerType) {
        return switch (controllerType) {
            case ATIS -> "atis_color";
            case DEL -> "delivery_color";
            case GND -> "ground_color";
            case TWR -> "tower_color";
            case APP, DEP -> "approach_circle_color";
            default -> null;
        };
    }

    @Override
    protected void clear(final Airport oldValue) {
        super.clear(oldValue);
        oldValue.getControllers().removeListener(this::controllersChanged);
        controllersGrid.getChildren().clear();
    }

    @FXML
    private void setToFir(final MouseEvent event) {
        viewModel.setToFir();
    }
}
