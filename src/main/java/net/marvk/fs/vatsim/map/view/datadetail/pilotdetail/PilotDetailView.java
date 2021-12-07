package net.marvk.fs.vatsim.map.view.datadetail.pilotdetail;

import de.saxsys.mvvmfx.InjectResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import net.marvk.fs.vatsim.map.data.FlightInformationRegion;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.EtaToStringMapper;
import net.marvk.fs.vatsim.map.view.datadetail.clientdetail.ClientDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubView;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubViewModel;
import net.marvk.fs.vatsim.map.view.datadetail.flightplandetail.FlightPlanDetailView;

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class PilotDetailView extends DataDetailSubView<DataDetailSubViewModel<Pilot>, Pilot> {
    @FXML
    private Label firName;
    @FXML
    private Label firIcao;
    @FXML
    private Label eta;
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

    @InjectResourceBundle
    private ResourceBundle resourceBundle;

    private EtaToStringMapper etaToStringMapper;

    @Override
    public void initialize() {
        super.initialize();
        etaToStringMapper = new EtaToStringMapper(resourceBundle);
    }

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
                verticalSpeed,
                eta,
                firIcao,
                firName
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
        eta.textProperty().bind(Bindings.createStringBinding(
                () -> etaToStringMapper.map(pilot.getEta()),
                pilot.etaProperty()
        ));
        verticalSpeed.textProperty().bind(Bindings.createStringBinding(
                () -> fpmString(pilot),
                pilot.verticalSpeedProperty())
        );
        firIcao.textProperty().bind(Bindings.createStringBinding(
                () -> firIcao(pilot), pilot.flightInformationRegionBoundaries()
        ));

        firName.textProperty().bind(Bindings.createStringBinding(
                () -> firName(pilot), pilot.flightInformationRegionBoundaries()
        ));
    }

    private String firIcao(final Pilot pilot) {
        final ReadOnlyListProperty<FlightInformationRegionBoundary> firbs = pilot.flightInformationRegionBoundaries();
        if (firbs.isEmpty()) {
            firIcao.getStyleClass().remove("hyperlink-label");
            return "????";
        } else {
            firIcao.getStyleClass().add("hyperlink-label");
            return firbs.get(0).getIcao();
        }
    }

    private String firName(final Pilot pilot) {
        final ReadOnlyListProperty<FlightInformationRegionBoundary> firbs =
                pilot.flightInformationRegionBoundaries();
        if (firbs.isEmpty()) {
            return "";
        } else {
            final ReadOnlyListProperty<FlightInformationRegion> firs =
                    firbs.get(0).getFlightInformationRegions();
            if (firs.isEmpty()) {
                return "";
            } else {
                return firs.get(0).getName();
            }
        }
    }

    private String fpmString(final Pilot pilot) {
        final double vs = pilot.getVerticalSpeed();
        if (Double.compare(vs, Double.NaN) == 0) {
            return resourceBundle.getString("detail.pilot.unknown");
        }

        final String prefix = Math.signum(vs) >= 1 ? "+" : "";
        return "%s%dfpm".formatted(prefix, Math.round(vs));
    }

    @Override
    protected void clear(final Pilot oldValue) {
        super.clear(oldValue);
        flightPlanController.getViewModel().setData(null);
        clientController.getViewModel().setData(null);
    }

    public void goToFir(final MouseEvent event) {
        final Pilot data = viewModel.getData();
        if (data != null) {
            final ReadOnlyListProperty<FlightInformationRegionBoundary> boundaries = data.flightInformationRegionBoundaries();
            if (!boundaries.isEmpty()) {
                viewModel.setDataDetail(boundaries.get(0));
            }
        }
    }
}
