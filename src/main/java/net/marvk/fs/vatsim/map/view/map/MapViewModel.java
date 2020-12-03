package net.marvk.fs.vatsim.map.view.map;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.SettingsScope;
import net.marvk.fs.vatsim.map.view.StatusbarScope;
import net.marvk.fs.vatsim.map.view.painter.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final ObservableList<PainterExecutor<?>> painterExecutors;

    private final MapVariables mapVariables = new MapVariables();

    private final FilteredList<FlightInformationRegionBoundary> highlightedBoundaries;

    private final ObservableList<FlightInformationRegionBoundary> selectedFir = FXCollections.observableList(new ArrayList<>(1));

    @InjectScope
    private StatusbarScope statusbarScope;

    @InjectScope
    private SettingsScope settingsScope;

    private boolean isMouseInBounds(final FlightInformationRegionBoundary fir) {
        return fir.getPolygon().boundary().contains(mouseWorldPosition.get());
    }

    @Inject
    public MapViewModel(
            final ClientRepository clientRepository,
            final AirportRepository airportRepository,
            final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository,
            final InternationalDateLineRepository internationalDateLineRepository,
            final UpperInformationRegionRepository upperInformationRegionRepository,
            final NotificationCenter notificationCenter,
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

        this.world = world;

        this.painterExecutors = FXCollections.observableArrayList(
                new PainterExecutor<>("Background", new BackgroundPainter(mapVariables, Color.valueOf("17130a"))),
                new PainterExecutor<>("World", new WorldPainter(mapVariables, Color.valueOf("0f0c02")), this::world),
                new PainterExecutor<>("Date Line", new IdlPainter(mapVariables, Color.valueOf("3b3b3b")), () -> Collections
                        .singleton(internationalDateLine())),
                new PainterExecutor<>("Inactive Firs", new InactiveFirPainter(mapVariables), this::flightInformationRegionBoundaries),
                new PainterExecutor<>("Active Uirs", new ActiveUirPainter(mapVariables), upperInformationRegionRepository::list),
                new PainterExecutor<>("Active Firs", new ActiveFirPainter(mapVariables), this::flightInformationRegionBoundaries),
                new PainterExecutor<>("Selected Firs", new FirPainter(mapVariables, Color.RED, 2.5), () -> selectedFir),
                new PainterExecutor<>("Pilots", new PilotPainter(mapVariables), this::pilots),
                new PainterExecutor<>("Airports", new AirportPainter(mapVariables), this::airports)
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

        notificationCenter.subscribe("REPAINT", (key, payload) -> triggerRepaint());

        selectedFir.addListener((ListChangeListener<FlightInformationRegionBoundary>) c -> triggerRepaint());
        viewHeight.addListener((observable, oldValue, newValue) -> triggerRepaint());
        viewWidth.addListener((observable, oldValue, newValue) -> triggerRepaint());
        worldCenter.addListener((observable, oldValue, newValue) -> triggerRepaint());
        scale.addListener((observable, oldValue, newValue) -> triggerRepaint());
    }

    private void triggerRepaint() {
        publish("REPAINT");
    }

    public void initialize() {
        Bindings.bindContent(statusbarScope.highlightedFirs(), highlightedBoundaries);
        statusbarScope.mouseViewPositionProperty().bind(mouseViewPosition);
        statusbarScope.mouseWorldPositionProperty().bind(mouseWorldPosition);

        Bindings.bindContent(settingsScope.getPainters(), painterExecutors);
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    public ReadOnlyObjectProperty<Point2D> worldCenterProperty() {
        return worldCenter.getReadOnlyProperty();
    }

    public void setWorldCenter(final Point2D worldCenter) {
        final double x = ((worldCenter.getX() + 540) % 360) - 180;
        this.worldCenter.set(new Point2D(
                clamp(x, -180, 180),
                clamp(worldCenter.getY(), -90, 90)
        ));
    }

    private static double clamp(final double value, final double min, final double max) {
        return Math.max(Math.min(value, max), min);
    }

    public Point2D getWorldCenter() {
        return worldCenter.get();
    }

    public ObservableList<Pilot> pilots() {
        return clientRepository.pilots();
    }

    public ObservableList<Controller> controllers() {
        return clientRepository.controllers();
    }

    public ObservableList<FlightInformationRegionBoundary> flightInformationRegionBoundaries() {
        return flightInformationRegionBoundaryRepository.list();
    }

    public InternationalDateLine internationalDateLine() {
        return internationalDateLineRepository.list().get(0);
    }

    public ObservableList<Airport> airports() {
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

    public FilteredList<FlightInformationRegionBoundary> highlightedBoundaries() {
        return highlightedBoundaries;
    }

    public ObservableList<FlightInformationRegionBoundary> selectedFir() {
        return selectedFir;
    }

    public void setSelectedFir(final FlightInformationRegionBoundary fir) {
        selectedFir.clear();
        if (fir != null) {
            selectedFir.add(0, fir);
        }
    }

    public Point2D getMouseWorldPosition() {
        return mouseWorldPosition.get();
    }
}
