package net.marvk.fs.vatsim.map.view.map;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.util.Duration;
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
    private final NotificationCenter notificationCenter;
    private final PositionDataVisitor positionDataVisitor;
    private final List<Polygon> world;
    private final ObservableList<PainterExecutor<?>> painterExecutors;

    private final MapVariables mapVariables = new MapVariables();

    private final ContextMenuViewModel contextMenu = new ContextMenuViewModel();

    private final ObjectProperty<Data> selectedItem = new SimpleObjectProperty<>();

    @InjectScope
    private StatusbarScope statusbarScope;

    @InjectScope
    private SettingsScope settingsScope;

    private WorldPanTransition panTransition = null;

    @Inject
    public MapViewModel(
            final ClientRepository clientRepository,
            final AirportRepository airportRepository,
            final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository,
            final InternationalDateLineRepository internationalDateLineRepository,
            final UpperInformationRegionRepository upperInformationRegionRepository,
            final NotificationCenter notificationCenter,
            final PositionDataVisitor positionDataVisitor,
            @Named("world") final List<Polygon> world
    ) {
        this.clientRepository = clientRepository;
        this.airportRepository = airportRepository;
        this.flightInformationRegionBoundaryRepository = flightInformationRegionBoundaryRepository;
        this.internationalDateLineRepository = internationalDateLineRepository;
        this.upperInformationRegionRepository = upperInformationRegionRepository;
        this.notificationCenter = notificationCenter;
        this.positionDataVisitor = positionDataVisitor;

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
        notificationCenter.subscribe("PAN_TO_POSITION", (key, payload) -> panToPosition(payload));

        this.selectedItem.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.viewHeight.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.viewWidth.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.worldCenter.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.scale.addListener((observable, oldValue, newValue) -> triggerRepaint());
    }

    private void panToPosition(final Object[] payload) {
        if (payload.length > 0) {
            if (payload[0] instanceof Point2D) {
                final Point2D p = ((Point2D) payload[0]).multiply(-1);
                panToPosition(new WorldPanTransition(
                        p,
                        p,
                        1,
                        32
                ));
            }
        }
    }

    private void panToPosition(final WorldPanTransition transition) {
        fireTransition(transition);
    }

    private void fireTransition(final WorldPanTransition transition) {
        if (panTransition != null) {
            if (panTransition.getStatus() == Animation.Status.RUNNING) {
                panTransition.stop();
            }
        }

        panTransition = transition;
        panTransition.playFromStart();
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

    public Data getSelectedItem() {
        return selectedItem.get();
    }

    public void setSelectedItem(final Data item) {
        selectedItem.set(item);
    }

    public ObjectProperty<Data> selectedItemProperty() {
        return selectedItem;
    }

    public Point2D getMouseWorldPosition() {
        return mouseWorldPosition.get();
    }

    public ContextMenuViewModel getContextMenu() {
        return contextMenu;
    }

    public void goToItem() {
        positionDataVisitor.visit(getSelectedItem()).ifPresent(p -> panToPosition(new WorldPanTransition(
                getWorldCenter(),
                p.multiply(-1),
                scale.get(),
                32
        )));
    }

    private class WorldPanTransition extends Transition {
        private final Point2D startingWorldCenter;
        private final double startingScale;

        private final Point2D targetWorldCenter;
        private final double targetScale;

        public WorldPanTransition(final Point2D startingWorldCenter, final Point2D targetWorldCenter, final double startingScale, final double targetScale) {
            super(30);
            setCycleDuration(Duration.seconds(1));
            setCycleCount(1);
            setInterpolator(Interpolator.EASE_BOTH);
            this.targetWorldCenter = targetWorldCenter;
            this.targetScale = targetScale;

            this.startingWorldCenter = startingWorldCenter;
            this.startingScale = startingScale;
        }

        @Override
        protected void interpolate(final double frac) {
//            worldCenter.set(position(frac));
//            scale.set(scale(frac));
            final double f1 = frac;
            worldCenter.set(position(f1));

            scale.set(scale(f1 * f1));
        }

//        private double scale(final double frac) {
//            final double s = startingScale;
//            final double t = targetScale;
//
//            final double u;
//
////            System.out.println("s = " + s);
////            System.out.println("t = " + t);
//
//            if (s == t && t - 1 != 0) {
//                u = 1. / 2;
//            } else {
//                final double p = Math.sqrt((s - 1) * (t - 1)) + s - 1;
//                if (s - t != 0 && -p != 0) {
//                    u = (-p) / (s - t);
//                } else if (p != 0) {
//                    u = p / (s - t);
//                } else {
//                    throw new IllegalStateException();
//                }
//            }
//
////            System.out.println("u = " + u);
//
//            final double x;
//            final double y;
//            final double z;
//            if (s == 0) {
//                x = t - 1;
//                y = 0;
//                z = 1;
//            } else {
//                x = (s - 1) / (u * u);
//                y = (2 - 2 * s) / (u);
//                z = s;
//            }
//
////            System.out.println("x = " + x);
////            System.out.println("y = " + y);
////            System.out.println("z = " + z);
//
//            final double r = frac * frac * x + frac * y + z;
////            System.out.println("r = " + r);
//            return r;
//        }

        private double scale(final double f1) {
            final double f0 = 1 - f1;
            return f0 * startingScale + f1 * targetScale;
        }

        private Point2D position(final double f1) {
            final double f0 = 1 - f1;
            return startingWorldCenter.multiply(f0).add(targetWorldCenter.multiply(f1));
        }
    }
}
