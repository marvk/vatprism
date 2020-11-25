package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.map.GeomUtil;
import net.marvk.fs.vatsim.map.repository.AirportRepository;
import net.marvk.fs.vatsim.map.repository.FlightInformationRegionBoundaryRepository;
import net.marvk.fs.vatsim.map.repository.FlightInformationRegionRepository;
import net.marvk.fs.vatsim.map.repository.UpperInformationRegionRepository;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class ControllerDataViewModel extends SimpleDataViewModel<VatsimClient, ControllerDataViewModel> implements ViewModel {
    private final ObjectProperty<ControllerType> controllerType = new SimpleObjectProperty<>();

    private final StringProperty infix = new SimpleStringProperty();

    private final AirportViewModel airport;
    private final AirportRepository airportRepository;

    private final UpperInformationRegionViewModel upperInformationRegion;
    private final UpperInformationRegionRepository upperInformationRegionRepository;

    private final FlightInformationRegionViewModel flightInformationRegion;
    private final FlightInformationRegionRepository flightInformationRegionRepository;

    private final FlightInformationRegionBoundaryViewModel flightInformationRegionBoundary;
    private final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository;

    @Inject
    public ControllerDataViewModel(
            final FlightInformationRegionBoundaryViewModel flightInformationRegionBoundary,
            final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository,
            final FlightInformationRegionViewModel flightInformationRegion,
            final FlightInformationRegionRepository flightInformationRegionRepository,
            final AirportViewModel airport,
            final AirportRepository airportRepository,
            final UpperInformationRegionViewModel upperInformationRegion,
            final UpperInformationRegionRepository upperInformationRegionRepository
    ) {
                this.flightInformationRegionBoundary = flightInformationRegionBoundary;
        this.flightInformationRegionBoundaryRepository = flightInformationRegionBoundaryRepository;
        this.flightInformationRegion = flightInformationRegion;
        this.flightInformationRegionRepository = flightInformationRegionRepository;
        this.airport = airport;
        this.airportRepository = airportRepository;
        this.upperInformationRegion = upperInformationRegion;
        this.upperInformationRegionRepository = upperInformationRegionRepository;

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

        final String callsign = vatsimClient.getCallsign();
        final String cid = vatsimClient.getCid();

        final String[] sections = callsign.split("_");
        final int n = sections.length;

        final ControllerType defaultControllerType = "ATC".equals(vatsimClient.getClientType()) ? ControllerType.OBS : ControllerType.NONE;

        final String identifier;
        final String infix;
        final ControllerType controllerType;

        if (n <= 1) {
            identifier = null;
            infix = null;
            controllerType = defaultControllerType;
        } else if (n == 2) {
            identifier = sections[0];
            infix = null;
            final String s1 = sections[1];
            controllerType = ControllerType.fromString(s1, defaultControllerType);
        } else if (n == 3) {
            identifier = sections[0];
            infix = sections[1];
            controllerType = ControllerType.fromString(sections[2], defaultControllerType);
        } else {
            log.warn("Unexpected callsign " + callsign);
            setEmpty();
            return;
        }

        final AirportViewModel ap;
        final FlightInformationRegionBoundaryViewModel firb = null;
        final UpperInformationRegionViewModel uir;
        final FlightInformationRegionViewModel fir;

        if (controllerType == ControllerType.OBS) {
            ap = null;
            fir = null;
            uir = null;
        } else {
            if (controllerType != ControllerType.CTR && controllerType != ControllerType.FSS) {
                ap = getAirport(vatsimClient, identifier);

                if (ap == null) {
                    log.warn("UNKNOWN AIRPORT: " + identifier + " Full callsign: " + callsign + ", cid " + cid);
                }

                uir = null;
                fir = null;
            } else {
                ap = null;
                uir = getUir(vatsimClient, identifier);

                if (uir == null) {
                    fir = getFir(vatsimClient, identifier, infix);

                    if (fir == null) {
                        log.warn("UNKNOWN UIR or FIR: " + identifier + " Full callsign: " + callsign + ", cid " + cid);
                    }
                } else {
                    fir = null;
                }
            }
        }

        set(
                controllerType,
                infix,
                ap,
                fir,
                firb,
                uir
        );
    }

    private FlightInformationRegionViewModel getFir(final VatsimClient vatsimClient, final String identifier, final String infix) {
        final List<FlightInformationRegionViewModel> firs = flightInformationRegionRepository.getByIdentifierAndInfix(identifier, infix);

        if (firs.isEmpty()) {
            return null;
        }

        if (firs.size() > 1) {
            log.warn("Could not determine exact FIR for " + identifier + ", full callsign " + vatsimClient.getCallsign() + ", cid " + vatsimClient
                    .getCid());
        }

        return firs.get(0);
    }

    private UpperInformationRegionViewModel getUir(final VatsimClient vatsimClient, final String identifier) {
        return returnFirst(
                () -> extractViewModel(
                        upperInformationRegionRepository.getByIcao(identifier),
                        e -> new Point2D(0., 0.),
                        vatsimClient
                )
        );
    }

    private FlightInformationRegionBoundaryViewModel getFirb(final VatsimClient vatsimClient, final String identifier, final String infix) {
        return returnFirst(
                () -> extractViewModel(
                        flightInformationRegionBoundaryRepository.getByIcao(identifier),
                        e -> e.centerPositionProperty().get(),
                        vatsimClient
                )
        );
    }

    private AirportViewModel getAirport(final VatsimClient vatsimClient, final String identifier) {
        return returnFirst(
                () -> extractViewModel(
                        airportRepository.getByIcao(identifier),
                        e -> e.positionProperty().get(),
                        vatsimClient
                ),
                () -> extractViewModel(
                        airportRepository.getByIataLid(identifier),
                        e -> e.positionProperty().get(),
                        vatsimClient
                )
        );
    }

    @SafeVarargs
    private static <ViewModel> ViewModel returnFirst(final Supplier<ViewModel>... supplier) {
        return Arrays.stream(supplier).map(Supplier::get).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private static <ViewModel> ViewModel extractViewModel(final List<ViewModel> viewModels, final Function<ViewModel, Point2D> positionExtractor, final VatsimClient vatsimClient) {
        if (viewModels.isEmpty()) {
            return null;
        }

        if (viewModels.size() == 1) {
            return viewModels.get(0);
        }

        final Point2D other = vatsimClientPosition(vatsimClient);

        return viewModels
                .stream()
                .min(Comparator.comparingDouble(e -> GeomUtil.distanceOnMsl(positionExtractor.apply(e), other)))
                .get();

    }

    private static Point2D vatsimClientPosition(final VatsimClient vatsimClient) {
        return GeomUtil.parsePoint(
                vatsimClient.getLongitude(),
                vatsimClient.getLatitude()
        );
    }

    private void setEmpty() {
        set(ControllerType.NONE, null, null, null, null, null);
    }

    private void set(final ControllerType controllerType,
                     final String infix,
                     final AirportViewModel airport,
                     final FlightInformationRegionViewModel flightInformationRegion,
                     final FlightInformationRegionBoundaryViewModel flightInformationRegionBoundary,
                     final UpperInformationRegionViewModel upperInformationRegion
    ) {

        this.controllerType.set(controllerType);
        this.infix.set(infix);
        this.airport.setModelFromViewModel(airport);
        this.flightInformationRegion.setModelFromViewModel(flightInformationRegion);
        this.flightInformationRegionBoundary.setModelFromViewModel(flightInformationRegionBoundary);
        this.upperInformationRegion.setModelFromViewModel(upperInformationRegion);
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

    public FlightInformationRegionBoundaryViewModel flightInformationRegionBoundary() {
        return flightInformationRegionBoundary;
    }

    public FlightInformationRegionViewModel flightInformationRegion() {
        return flightInformationRegion;
    }

    public UpperInformationRegionViewModel upperInformationRegion() {
        return upperInformationRegion;
    }
}
