package net.marvk.fs.vatsim.map.view.datadetail.trafficdetail;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import net.marvk.fs.vatsim.map.data.FlightPlan;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.NoneSelectionModel;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailPane;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DetailSubView;

import java.util.List;
import java.util.Locale;

public class TrafficDetailView extends DetailSubView<TrafficDetailViewModel, ObservableList<FlightPlan>> {
    @FXML
    private ListView<FlightPlan> list;
    @FXML
    private DataDetailPane container;

    private final Label titleLabel = new Label();
    private final Label queryLabel = new Label();
    private final TextField queryTextField = new TextField();

    @Override
    public void initialize() {
        super.initialize();
        list.setCellFactory(l -> new PilotListCell(viewModel));
        list.setSelectionModel(new NoneSelectionModel<>());
        list.setFocusTraversable(true);

        container.setOnKeyTyped(this::handleKeyTyped);
        container.setOnKeyPressed(this::handleKeyPressed);

        list.setOnMouseClicked(e -> container.requestFocus());

        container.setHeaderNode(setupStackPane());
        viewModel.queryProperty().bind(queryTextField.textProperty());
        queryLabel.textProperty().bind(viewModel.queryProperty());
        queryTextField.caretPositionProperty().addListener((observable, oldValue, newValue) ->
                queryTextField.positionCaret(Integer.MAX_VALUE)
        );
        queryTextField.selectionProperty().addListener((observable, oldValue, newValue) -> {
            queryTextField.deselect();
            queryTextField.positionCaret(Integer.MAX_VALUE);
        });
    }

    private void handleKeyTyped(final KeyEvent keyEvent) {
        queryTextField.fireEvent(keyEvent);
    }

    private void handleKeyPressed(final KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            clearQuery();
        } else if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
            queryTextField.fireEvent(keyEvent);
        }
    }

    private Node setupStackPane() {
        final StackPane header = new StackPane();
        queryTextField.setVisible(false);
        queryTextField.setMaxWidth(0);
        queryTextField.setMaxHeight(0);
        titleLabel.getStyleClass().add("header-label");
        queryLabel.getStyleClass().add("header-search");
        StackPane.setMargin(queryLabel, new Insets(0, 0, 0, 2));
        header.getChildren().addAll(titleLabel, queryTextField, queryLabel);
        StackPane.setAlignment(queryLabel, Pos.CENTER_LEFT);
        return header;
    }

    @Override
    protected List<Label> labels() {
        return List.of(
                titleLabel
        );
    }

    @Override
    protected List<StringProperty> stringProperties() {
        return List.of(queryTextField.textProperty());
    }

    @Override
    protected void setData(final ObservableList<FlightPlan> data) {
        titleLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> "%s (%s)".formatted(getTitle(), data.size()),
                        data
                )
        );

        list.setItems(viewModel.getFilteredSortedData());
        list.prefHeightProperty().bind(PilotListCell.height.multiply(7));
        list.setPlaceholder(new Label("No " + getTitle().toLowerCase(Locale.ROOT)));
        clearQuery();
    }

    private void clearQuery() {
        queryTextField.textProperty().set("");
    }

    private String getTitle() {
        if (viewModel.getTitle() == null || viewModel.getTitle().isEmpty()) {
            if (viewModel.getType() != null) {
                return viewModel.getType().label;
            } else {
                return "";
            }
        } else {
            return viewModel.getTitle();
        }
    }

    private static String flightNumber(final Pilot pilot, final int length) {
        if (pilot.isFlightNumberAvailable()) {
            final String icao = pilot.getAirline().getIcao();
            final String flightNumber = pilot.getFlightNumber();

            return " ".repeat(3 - icao.length()) + icao + " ".repeat((length - 3) - flightNumber.length()) + flightNumber;
        } else {
            return " ".repeat(length - pilot.getCallsign().length()) + pilot.getCallsign();
        }
    }

    public enum Type {
        ARRIVAL("Arrivals"), DEPARTURE("Departures");

        private final String label;

        Type(final String label) {
            this.label = label;
        }

    }

    private static class PilotListCell extends ListCell<FlightPlan> {
        private static final DoubleProperty height = new SimpleDoubleProperty(15);

        private final Label label;
        private final VBox container;
        private final HBox holder;

        private FlightPlan flightPlan = null;

        public PilotListCell(final TrafficDetailViewModel viewModel) {
            label = new Label();
            label.getStyleClass().addAll("mono", "hyperlink-label");
            label.setOnMouseClicked(e -> viewModel.setDataDetail(flightPlan.getPilot()));

            holder = new HBox();
            holder.getChildren().add(label);

            container = new VBox();
            container.getChildren().add(holder);

            holder.heightProperty().addListener((observable, oldValue, newValue) -> {
                if (flightPlan != null) {
                    height.set(newValue.doubleValue());
                }
            });

            prefHeightProperty().bind(height);
        }

        @Override
        protected void updateItem(final FlightPlan flightPlan, final boolean empty) {
            super.updateItem(flightPlan, empty);

            this.flightPlan = flightPlan;

            if (empty || flightPlan == null) {
                setText(null);
                setGraphic(null);
            } else {
                final Pilot pilot = flightPlan.getPilot();

                label.setText(flightNumber(pilot, 10));
                setGraphic(container);
            }
        }
    }

}

