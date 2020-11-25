package net.marvk.fs.vatsim.map.view.map;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.repository.*;
import net.marvk.fs.vatsim.map.view.StatusbarScope;
import net.marvk.fs.vatsim.map.view.painter.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ScopeProvider(StatusbarScope.class)
public class MapViewModel implements ViewModel {
    private final DoubleProperty scale = new SimpleDoubleProperty(1);
    private final ReadOnlyObjectWrapper<Point2D> worldCenter = new ReadOnlyObjectWrapper<>(new Point2D(0, 0));
    private final DoubleProperty viewWidth = new SimpleDoubleProperty();
    private final DoubleProperty viewHeight = new SimpleDoubleProperty();

    private final ObjectProperty<Point2D> mouseViewPosition = new SimpleObjectProperty<>(new Point2D(0, 0));
    private final ObjectProperty<Point2D> mouseWorldPosition = new SimpleObjectProperty<>(new Point2D(0, 0));

    private final ClientRepository clientRepository;
    private final AirportRepository airportRepository;
    private final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository;

    private final InternationalDateLineRepository internationalDateLineRepository;
    private final UpperInformationRegionRepository upperInformationRegionRepository;
    private final List<Polygon> world;
    private final List<PainterExecutor<?>> painterExecutors;

    private final MapVariables mapVariables = new MapVariables();

    private final FilteredList<FlightInformationRegionBoundaryViewModel> highlightedBoundaries;
    private final FilteredList<FlightInformationRegionBoundaryViewModel> onlineFirs;

    private final FilteredList<UpperInformationRegionViewModel> onlineUirs;

    @InjectScope
    private StatusbarScope statusbarScope;

    private boolean isMouseInBounds(final FlightInformationRegionBoundaryViewModel fir) {
        return fir.getPolygon().boundary().contains(mouseWorldPosition.get());
    }

    private boolean isFirOnline(final FlightInformationRegionBoundaryViewModel fir) {
        final Set<String> onlineIcaos = controllers()
                .stream()
                .map(ClientViewModel::controllerData)
                .map(ControllerDataViewModel::flightInformationRegion)
                .map(FlightInformationRegionViewModel::icaoProperty)
                .map(ObservableObjectValue::get)
                .collect(Collectors.toSet());

        return onlineIcaos.contains(fir.icaoProperty().get());
    }

    private boolean isUirOnline(final UpperInformationRegionViewModel uir) {
        final Set<String> onlineIcaos = controllers()
                .stream()
                .map(ClientViewModel::controllerData)
                .map(ControllerDataViewModel::upperInformationRegion)
                .map(UpperInformationRegionViewModel::icaoProperty)
                .map(ObservableObjectValue::get)
                .collect(Collectors.toSet());

        return onlineIcaos.contains(uir.icaoProperty().get());
    }

    @Inject
    public MapViewModel(
            final ClientRepository clientRepository,
            final AirportRepository airportRepository,
            final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository,
            final InternationalDateLineRepository internationalDateLineRepository,
            final UpperInformationRegionRepository upperInformationRegionRepository,
            @Named("world") final List<Polygon> world
    ) {

        this.clientRepository = clientRepository;
        this.airportRepository = airportRepository;
        this.flightInformationRegionBoundaryRepository = flightInformationRegionBoundaryRepository;
        this.internationalDateLineRepository = internationalDateLineRepository;
        this.upperInformationRegionRepository = upperInformationRegionRepository;
        this.highlightedBoundaries = new FilteredList<>(flightInformationRegionBoundaries(), this::isMouseInBounds);
        this.highlightedBoundaries.predicateProperty().bind(Bindings.createObjectBinding(
                () -> this::isMouseInBounds,
                mouseWorldPosition
        ));

        this.onlineFirs = new FilteredList<>(flightInformationRegionBoundaries(), this::isFirOnline);
        this.onlineFirs.predicateProperty().bind(Bindings.createObjectBinding(
                () -> this::isFirOnline
        ));

        this.onlineUirs = new FilteredList<>(upperInformationRegionRepository.list(), this::isUirOnline);
        this.onlineUirs.predicateProperty().bind(Bindings.createObjectBinding(
                () -> this::isUirOnline
        ));

        this.world = world;

        this.painterExecutors = List.of(
                new PainterExecutor<>("Background", new BackgroundPainter(mapVariables, Color.valueOf("17130a"))),
                new PainterExecutor<>("World", new WorldPainter(mapVariables, Color.valueOf("0f0c02")), this::world),
                new PainterExecutor<>("Date Line", new IdlPainter(mapVariables, Color.valueOf("3b3b3b")), () -> Collections
                        .singletonList(internationalDateLine())),
//                new PainterExecutor<>("Airports", new AirportPainter(mapVariables), this::airports),
                new PainterExecutor<>("Firs", new FirPainter(mapVariables, Color.rgb(59, 52, 31, 0.5), 0.5, true), this::flightInformationRegionBoundaries),
                new PainterExecutor<>("Online Uirs", new UirPainter(mapVariables, new FirPainter(mapVariables, Color.LIGHTBLUE, 1.5)), () -> onlineUirs),
                new PainterExecutor<>("Online Firs", new FirPainter(mapVariables, Color.DEEPPINK, 2), () -> onlineFirs),
//                new PainterExecutor<>("Filtered Firs", new FirPainter(mapVariables, Color.RED, 0.5), () -> highlightedBoundaries),
                new PainterExecutor<>("Pilots", new PilotPainter(mapVariables), this::pilots)
        );

        mouseViewPosition.addListener((observable, oldValue, newValue) -> mouseWorldPosition.set(mapVariables.toWorld(newValue)));

        scale.addListener((observable, oldValue, newValue) -> mapVariables.setScale(newValue.doubleValue()));
        mapVariables.setScale(scale.get());
        worldCenter.addListener((observable, oldValue, newValue) -> mapVariables.setWorldCenter(newValue));
        mapVariables.setWorldCenter(worldCenter.get());
        viewWidth.addListener((observable, oldValue, newValue) -> mapVariables.setViewWidth(newValue.doubleValue()));
        mapVariables.setViewWidth(viewWidth.get());
        viewHeight.addListener((observable, oldValue, newValue) -> mapVariables.setViewHeight(newValue.doubleValue()));
        mapVariables.setViewHeight(viewHeight.get());

    }

    public void initialize() {
        Bindings.bindContent(statusbarScope.highlightedFirs(), highlightedBoundaries);
        statusbarScope.mouseViewPositionProperty().bind(mouseViewPosition);
        statusbarScope.mouseWorldPositionProperty().bind(mouseWorldPosition);
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    public ReadOnlyObjectProperty<Point2D> worldCenterProperty() {
        return worldCenter.getReadOnlyProperty();
    }

    public void setWorldCenter(final Point2D worldCenter) {
        final double x = ((worldCenter.getX() + 540) % 360) - 180;
        this.worldCenter.set(new Point2D(x, worldCenter.getY()));
    }

    public Point2D getWorldCenter() {
        return worldCenter.get();
    }

    public ObservableList<ClientViewModel> pilots() {
        return clientRepository.pilots();
    }

    public ObservableList<ClientViewModel> controllers() {
        return clientRepository.controllers();
    }

    public ObservableList<FlightInformationRegionBoundaryViewModel> flightInformationRegionBoundaries() {
        return flightInformationRegionBoundaryRepository.list();
    }

    public InternationalDateLineViewModel internationalDateLine() {
        return internationalDateLineRepository.list().get(0);
    }

    public ObservableList<AirportViewModel> airports() {
        return airportRepository.list();
    }

    public List<Polygon> world() {
        return world;
    }

    public DoubleProperty viewWidthProperty() {
        return viewWidth;
    }

    public DoubleProperty viewHeightProperty() {
        return viewHeight;
    }

    public List<PainterExecutor<?>> getPainterExecutors() {
        return painterExecutors;
    }

    public ObjectProperty<Point2D> mouseViewPositionProperty() {
        return mouseViewPosition;
    }
}
