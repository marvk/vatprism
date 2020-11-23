package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.map.repository.AirportRepository;
import net.marvk.fs.vatsim.map.repository.FlightInformationRegionBoundaryRepository;

@Slf4j
public class ControllerDataViewModel extends SimpleDataViewModel<VatsimClient, ControllerDataViewModel> implements ViewModel {
    private final ObjectProperty<ControllerType> controllerType = new SimpleObjectProperty<>();
    private final StringProperty infix = new SimpleStringProperty();
    private final AirportViewModel airport;
    private final AirportRepository airportRepository;
    private final FlightInformationRegionBoundaryViewModel flightInformationRegion;
    private final StringProperty uir = new SimpleStringProperty();

    private final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository;

    @Inject
    public ControllerDataViewModel(
            final FlightInformationRegionBoundaryViewModel flightInformationRegionBoundary,
            final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository,
            final AirportViewModel airport,
            final AirportRepository airportRepository
    ) {
        this.flightInformationRegion = flightInformationRegionBoundary;
        this.flightInformationRegionBoundaryRepository = flightInformationRegionBoundaryRepository;
        this.airport = airport;
        this.airportRepository = airportRepository;

        setupBindings();
    }

    private void setupBindings() {
        modelProperty().addListener((observable, oldValue, newValue) -> update(newValue));
    }

    private void update(final VatsimClient vatsimClient) {
        if (vatsimClient == null || vatsimClient.getCallsign() == null || !"ATC".equals(vatsimClient.getClientType())) {
            setEmpty();
            return;
        }

        final String[] sections = vatsimClient.getCallsign().split("_");
        final int n = sections.length;

        final ControllerType controllerType;
        final String infix;
        final String location;
        if (n <= 1) {
            controllerType = ControllerType.OBS;
            infix = null;
            location = null;
        } else if (n == 2) {
            location = sections[0];
            infix = null;
            final String s1 = sections[1];
            controllerType = ControllerType.fromString(s1);
        } else if (n == 3) {
            location = sections[0];
            infix = sections[1];
            controllerType = ControllerType.fromString(sections[2]);
        } else {
            log.warn("Unexpected callsign " + vatsimClient.getCallsign());
            setEmpty();
            return;
        }

        set(
                controllerType,
                infix,
                airportRepository.getByKey(location),
                flightInformationRegionBoundaryRepository.getByKey(location),
                null
        );
    }

    private void setEmpty() {
        set(ControllerType.NONE, null, null, null, null);
    }

    private void set(final ControllerType controllerType, final String infix, final AirportViewModel airport, final FlightInformationRegionBoundaryViewModel flightInformationRegion, final String uir) {
        this.controllerType.set(controllerType);
        this.infix.set(infix);
        this.airport.setModelFromViewModel(airport);
        this.flightInformationRegion.setModelFromViewModel(flightInformationRegion);
        this.uir.set(uir);
    }

    public ObjectProperty<ControllerType> controllerTypeProperty() {
        return controllerType;
    }

    public StringProperty infixProperty() {
        return infix;
    }

    public AirportViewModel airport() {
        return airport;
    }

    public FlightInformationRegionBoundaryViewModel fir() {
        return flightInformationRegion;
    }

    public StringProperty uirProperty() {
        return uir;
    }
}
