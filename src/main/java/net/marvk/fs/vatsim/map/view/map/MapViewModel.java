package net.marvk.fs.vatsim.map.view.map;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.SettingsScope;
import net.marvk.fs.vatsim.map.view.StatusbarScope;
import net.marvk.fs.vatsim.map.view.painter.*;

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

    private final ContextMenuViewModel contextMenu = new ContextMenuViewModel();

    private final ObjectProperty<Data> selectedItem = new SimpleObjectProperty<>();

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

        this.mouseWorldPosition.addListener((observable, oldValue, newValue) -> setContextMenuItems(newValue));

        this.world = world;

        this.painterExecutors = executors(upperInformationRegionRepository);

        this.mouseViewPosition.addListener((observable, oldValue, newValue) -> mouseWorldPosition.set(mapVariables.toWorld(newValue)));

        this.scale.addListener((observable, oldValue, newValue) -> mapVariables.setScale(newValue.doubleValue()));
        this.mapVariables.setScale(scale.get());
        this.worldCenter.addListener((observable, oldValue, newValue) -> mapVariables.setWorldCenter(newValue));
        this.mapVariables.setWorldCenter(worldCenter.get());
        this.viewWidth.addListener((observable, oldValue, newValue) -> mapVariables.setViewWidth(newValue.doubleValue()));
        this.mapVariables.setViewWidth(viewWidth.get());
        this.viewHeight.addListener((observable, oldValue, newValue) -> mapVariables.setViewHeight(newValue.doubleValue()));
        this.mapVariables.setViewHeight(viewHeight.get());

        notificationCenter.subscribe("REPAINT", (key, payload) -> triggerRepaint());

        this.selectedItem.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.viewHeight.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.viewWidth.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.worldCenter.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.scale.addListener((observable, oldValue, newValue) -> triggerRepaint());
    }

    private void setContextMenuItems(final Point2D mouseWorldPosition) {
        contextMenu.getBoundaries()
                   .getItems()
                   .setAll(flightInformationRegionBoundaryRepository.getByPosition(mouseWorldPosition));

        contextMenu.getAirports()
                   .getItems()
                   .setAll(airportRepository.searchByPosition(mouseWorldPosition, 1));

        contextMenu.getPilots()
                   .getItems()
                   .setAll(clientRepository.searchByPosition(mouseWorldPosition, 1));
    }

    private ObservableList<PainterExecutor<?>> executors(final UpperInformationRegionRepository upperInformationRegionRepository) {
        return FXCollections.observableArrayList(
                PainterExecutor.of("Background", new BackgroundPainter(mapVariables, Color.valueOf("17130a"))),
                PainterExecutor.ofCollection("World", new WorldPainter(mapVariables, Color.valueOf("0f0c02")), this::world),
                PainterExecutor.ofItem("Date Line", new IdlPainter(mapVariables, Color.valueOf("3b3b3b")), this::internationalDateLine),
                PainterExecutor.ofCollection("Inactive Firs", new InactiveFirPainter(mapVariables), this::flightInformationRegionBoundaries),
                PainterExecutor.ofCollection("Active Uirs", new ActiveUirPainter(mapVariables), upperInformationRegionRepository::list),
                PainterExecutor.ofCollection("Active Firs", new ActiveFirPainter(mapVariables), this::flightInformationRegionBoundaries),
                PainterExecutor.ofItem("Selected Firs", new FirPainter(mapVariables, Color.RED, 2.5), this::selectedFirb),
                PainterExecutor.ofCollection("Pilots", new PilotPainter(mapVariables), this::pilots),
                PainterExecutor.ofCollection("Airports", new AirportPainter(mapVariables), this::airports)
        );
    }

    private FlightInformationRegionBoundary selectedFirb() {
        return selectedItem.get() instanceof FlightInformationRegionBoundary
                ? (FlightInformationRegionBoundary) selectedItem.get()
                : null;
    }

    private Airport selectedAirport() {
        return selectedItem.get() instanceof Airport
                ? (Airport) selectedItem.get()
                : null;
    }

    private void triggerRepaint() {
        publish("REPAINT");
    }

    public void initialize() {
        Bindings.bindContent(statusbarScope.highlightedFirs(), contextMenu.getBoundaries().getItems());
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

    public ReadOnlyObjectProperty<Data> selectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(final Data item) {
        selectedItem.set(item);
    }

    public Point2D getMouseWorldPosition() {
        return mouseWorldPosition.get();
    }

    public ContextMenuViewModel getContextMenu() {
        return contextMenu;
    }
}
