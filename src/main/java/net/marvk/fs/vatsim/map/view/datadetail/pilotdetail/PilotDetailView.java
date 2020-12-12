package net.marvk.fs.vatsim.map.view.datadetail.pilotdetail;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.datadetail.clientdetail.ClientDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubView;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubViewModel;
import net.marvk.fs.vatsim.map.view.datadetail.flightplandetail.FlightPlanDetailView;

import java.util.Collections;
import java.util.List;

public class PilotDetailView extends DataDetailSubView<DataDetailSubViewModel<Pilot>, Pilot> {
    @FXML
    private Label verticalSpeed;
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
                altitude,
                verticalSpeed
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
        verticalSpeed.textProperty().bind(Bindings.createStringBinding(
                () -> fpmString(pilot),
                pilot.verticalSpeedProperty())
        );
    }

    private static String fpmString(final Pilot pilot) {
        final double vs = pilot.getVerticalSpeed();
        if (Double.compare(vs, Double.NaN) == 0) {
            return "Unknown";
        }

        final String prefix = Math.signum(vs) >= 1 ? "+" : "";
        return prefix + Math.round(vs) + "fpm";
    }

    @Override
    protected void clear(final Pilot oldValue) {
        super.clear(oldValue);
        flightPlanController.getViewModel().setData(null);
        clientController.getViewModel().setData(null);
    }
}
