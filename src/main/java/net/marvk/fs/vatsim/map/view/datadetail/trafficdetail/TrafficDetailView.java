package net.marvk.fs.vatsim.map.view.datadetail.trafficdetail;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ListExpression;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.FlightPlan;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailPane;
import net.marvk.fs.vatsim.map.view.datadetail.PlaceholderBox;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DetailSubView;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class TrafficDetailView extends DetailSubView<TrafficDetailViewModel, ListExpression<FlightPlan>> {
    @FXML
    private GridPane trafficGrid;
    @FXML
    private PlaceholderBox trafficHolder;
    @FXML
    private DataDetailPane container;

    private final Label titleLabel = new Label();
    private final Label queryLabel = new Label();
    private final TextField queryTextField = new TextField();

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
    public void initialize() {
        super.initialize();

        container.setOnKeyTyped(this::handleKeyTyped);
        container.setOnKeyPressed(this::handleKeyPressed);

        trafficGrid.setOnMouseClicked(this::forwardMouseEvent);
        trafficHolder.setOnMouseClicked(this::forwardMouseEvent);

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

        viewModel.getFilteredSortedData().addListener(
                (ListChangeListener<FlightPlan>) c -> setGrid(viewModel.getFilteredSortedData())
        );

        trafficHolder.placeholderTextProperty().bind(Bindings.createStringBinding(
                () -> "No %s traffic".formatted(viewModel.getType() == Type.ARRIVAL ? "incoming" : "outgoing"),
                viewModel.typeProperty()
        ));
    }

    private void forwardMouseEvent(final MouseEvent e) {
        container.requestFocus();
        container.fireEvent(e);
    }

    @Override
    protected void setData(final ListExpression<FlightPlan> data) {
        titleLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> "%s (%s)".formatted(getTitle(), data.size()),
                        data
                )
        );

        trafficHolder.contentVisibleProperty().bind(data.emptyProperty().not());

        clearQuery();
    }

    private void setGrid(final ObservableList<FlightPlan> data) {
        trafficGrid.getChildren().clear();

        for (int i = 0; i < data.size(); i++) {
            final FlightPlan flightPlan = viewModel.getFilteredSortedData().get(i);
            final Label callsign = new Label(flightPlan.getPilot().getCallsign());
            callsign.getStyleClass().addAll("mono", "hyperlink-label");
            callsign.setOnMouseClicked(e -> viewModel.setDataDetail(flightPlan.getPilot()));
            trafficGrid.add(callsign, 0, i);

            final Label icao = new Label(extractFromOppositeAirport(flightPlan, Airport::getIcao));
            icao.getStyleClass().addAll("mono", "hyperlink-label");
            icao.setOnMouseClicked(e -> viewModel.setDataDetail(oppositeAirport(flightPlan)));
            trafficGrid.add(icao, 1, i);

            final Label airportName = new Label(extractFromOppositeAirport(flightPlan, e -> e.getNames().get(0).get()));
            trafficGrid.add(airportName, 2, i);

            final Label name = new Label(flightPlan.getPilot().getRealName());
            trafficGrid.add(name, 3, i);
        }
    }

//    private void initTable() {
//        table.setSelectionModel(new NoneSelectionModel<>(table));
//        table.setFocusTraversable(true);
//        table.getStyleClass().add("no-header");
//
//        final TableColumn<FlightPlan, String> c1 = new TableColumn<>();
//        c1.setCellValueFactory(param -> param.getValue().getPilot().callsignProperty());
//        c1.setCellFactory(param -> new FlightPlanTableCell(true, true));
//
//        final TableColumn<FlightPlan, String> c2 = new TableColumn<>();
//        c2.setCellValueFactory(param -> param.getValue().getPilot().realNameProperty());
//        c2.setCellFactory(param -> new FlightPlanTableCell(false, false));
//
//        final TableColumn<FlightPlan, String> c3 = new TableColumn<>();
//        c3.setCellValueFactory(param -> oppositeAirport(param).icaoProperty());
//        c3.setCellFactory(param -> new FlightPlanTableCell(true, false));
//
//        final TableColumn<FlightPlan, String> c4 = new TableColumn<>();
//        c4.setCellValueFactory(param -> oppositeAirport(param).getNames().get(0));
//        c4.setCellFactory(param -> new FlightPlanTableCell(false, false));
//
//        table.getColumns().add(c1);
//        table.getColumns().add(c3);
//        table.getColumns().add(c4);
//        table.getColumns().add(c2);
//    }

    private <E> E extractFromOppositeAirport(final FlightPlan flightPlan, final Function<Airport, E> stringFunction) {
        return Optional.ofNullable(oppositeAirport(flightPlan)).map(stringFunction).orElse(null);
    }

    private Airport oppositeAirport(final FlightPlan flightPlan) {
        if (viewModel.getType() == Type.ARRIVAL) {
            return flightPlan.getDepartureAirport();
        }
        return flightPlan.getArrivalAirport();
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

    //    private static class PilotListCell extends ListCell<FlightPlan> {
//        private static final DoubleProperty height = new SimpleDoubleProperty(15);
//
//        private final Label label;
//        private final VBox container;
//        private final HBox holder;
//
//        private FlightPlan flightPlan = null;
//
//        public PilotListCell(final TrafficDetailViewModel viewModel) {
//            label = new Label();
//            label.getStyleClass().addAll("mono", "hyperlink-label");
//            label.setOnMouseClicked(e -> viewModel.setDataDetail(flightPlan.getPilot()));
//
//            holder = new HBox();
//            holder.getChildren().add(label);
//
//            container = new VBox();
//            container.getChildren().add(holder);
//
//            holder.heightProperty().addListener((observable, oldValue, newValue) -> {
//                if (flightPlan != null) {
//                    height.set(newValue.doubleValue());
//                }
//            });
//
//            prefHeightProperty().bind(height);
//        }
//
//        @Override
//        protected void updateItem(final FlightPlan flightPlan, final boolean empty) {
//            super.updateItem(flightPlan, empty);
//
//            this.flightPlan = flightPlan;
//
//            if (empty || flightPlan == null) {
//                setText(null);
//                setGraphic(null);
//            } else {
//                final Pilot pilot = flightPlan.getPilot();
//
//                label.setText(flightNumber(pilot, 10));
//                setGraphic(container);
//            }
//        }
//    }

}

