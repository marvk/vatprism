package net.marvk.fs.vatsim.map.view.pilotdetail;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.clientdetail.ClientDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailSubView;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailSubViewModel;
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
    private FlightPlanDetailView flightPlanController;

    @FXML
    private ClientDetailView clientController;

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
        heading.textProperty().bind(doubleToIntString(pilot.headingProperty()));
        groundSpeed.textProperty().bind(doubleToIntString(pilot.groundSpeedProperty()));
        qnhMillibars.textProperty().bind(doubleToIntString(pilot.qnhMilliBarsProperty(), "mbar"));
        qnhInchesMercury.textProperty().bind(pilot.qnhInchesMercuryProperty().asString().concat("inHg"));
        squawk.textProperty().bind(pilot.transponderProperty());
        altitude.textProperty().bind(doubleToIntString(pilot.altitudeProperty(), "ft"));
        flightPlanController.getViewModel().setData(pilot.getFlightPlan());
        clientController.getViewModel().setData(pilot);
        clientController.getViewModel().setData(pilot);
    }

    private static StringBinding doubleToIntString(final ReadOnlyDoubleProperty property, final String suffix) {
        return Bindings.createStringBinding(
                () -> property.intValue() + suffix,
                property
        );
    }

    private static StringBinding doubleToIntString(final ReadOnlyDoubleProperty property) {
        return doubleToIntString(property, "");
    }
}
