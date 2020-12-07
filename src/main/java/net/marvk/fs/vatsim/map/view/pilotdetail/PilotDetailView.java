package net.marvk.fs.vatsim.map.view.pilotdetail;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.clientdetail.ClientDetailView;
import net.marvk.fs.vatsim.map.view.detailsubview.DataDetailSubView;
import net.marvk.fs.vatsim.map.view.detailsubview.DataDetailSubViewModel;
import net.marvk.fs.vatsim.map.view.flightplandetail.FlightPlanDetailView;

import java.util.Collections;
import java.util.List;

public class PilotDetailView extends DataDetailSubView<DataDetailSubViewModel<Pilot>, Pilot> {
    @FXML
    private TitledPane pilot;
    @FXML
    private TitledPane flight;
    @FXML
    private Label position;
    @FXML
    private Label heading;
    @FXML
    private Label groundSpeed;
    @FXML
    private Label qnhMillibars;
    @FXML
    private Label qnhInchesMercury;
    @FXML
    private Label squawk;
    @FXML
    private Label altitude;

    @FXML
    private ClientDetailView clientController;

    @FXML
    private FlightPlanDetailView flightPlanController;

    @Override
    protected List<TextArea> textAreas() {
        return Collections.emptyList();
    }

    @Override
    protected List<Label> labels() {
        return List.of(
                position,
                heading,
                groundSpeed,
                qnhMillibars,
                qnhInchesMercury,
                squawk,
                altitude
        );
    }

    @Override
    protected void setData(final Pilot pilot) {
        position.textProperty().bind(positionLabel(pilot.positionProperty()));
        heading.textProperty().bind(doubleToIntString(pilot.headingProperty(), "°"));
        groundSpeed.textProperty().bind(doubleToIntString(pilot.groundSpeedProperty(), "kts"));
        qnhMillibars.textProperty().bind(doubleToIntString(pilot.qnhMilliBarsProperty(), "mbar"));
        qnhInchesMercury.textProperty().bind(pilot.qnhInchesMercuryProperty().asString().concat("inHg"));
        squawk.textProperty().bind(pilot.transponderProperty());
        altitude.textProperty().bind(doubleToIntString(pilot.altitudeProperty(), "ft"));
        flightPlanController.getViewModel().setData(pilot.getFlightPlan());
        clientController.getViewModel().setData(pilot);
    }

    @Override
    protected void clear(final Pilot oldValue) {
        super.clear(oldValue);
        flightPlanController.getViewModel().setData(null);
        clientController.getViewModel().setData(null);
    }
}
