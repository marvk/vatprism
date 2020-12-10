package net.marvk.fs.vatsim.map.view.map;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.SettingsScope;
import net.marvk.fs.vatsim.map.view.StatusbarScope;
import net.marvk.fs.vatsim.map.view.painter.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final PositionDataVisitor positionDataVisitor;
    private final List<Polygon> world;
    private final ObservableList<PainterExecutor<?>> painterExecutors;

    private final MapVariables mapVariables = new MapVariables();

    private final ContextMenuViewModel contextMenu = new ContextMenuViewModel();

    private final ObjectProperty<Data> selectedItem = new SimpleObjectProperty<>();

    private final ObjectProperty<Object> selectionShape = new SimpleObjectProperty<>();

    private final FrameMetrics frameMetrics;

    @InjectScope
    private StatusbarScope statusbarScope;

    @InjectScope
    private SettingsScope settingsScope;

    private WorldPanTransition panTransition = null;

    private final TransitionDataVisitor transitionDataVisitor = new TransitionDataVisitor();

    @Inject
    public MapViewModel(
            final ClientRepository clientRepository,
            final AirportRepository airportRepository,
            final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository,
            final InternationalDateLineRepository internationalDateLineRepository,
            final UpperInformationRegionRepository upperInformationRegionRepository,
            final PositionDataVisitor positionDataVisitor,
            @Named("world") final List<Polygon> world
    ) {
        this.clientRepository = clientRepository;
        this.airportRepository = airportRepository;
        this.flightInformationRegionBoundaryRepository = flightInformationRegionBoundaryRepository;
        this.internationalDateLineRepository = internationalDateLineRepository;
        this.upperInformationRegionRepository = upperInformationRegionRepository;
        this.positionDataVisitor = positionDataVisitor;

        this.mouseWorldPosition.addListener((observable, oldValue, newValue) -> setContextMenuItems(newValue));

        this.world = world;

        this.painterExecutors = executors(upperInformationRegionRepository);

        final ArrayList<String> names = painterExecutors
                .stream()
                .map(PainterExecutor::getName)
                .collect(Collectors.toCollection(ArrayList::new));
        names.add(0, "Total");

        this.frameMetrics = new FrameMetrics(names, 250);

        this.mouseViewPosition.addListener((observable, oldValue, newValue) -> mouseWorldPosition.set(mapVariables.toWorld(newValue)));

        this.scale.addListener((observable, oldValue, newValue) -> mapVariables.setScale(newValue.doubleValue()));
        this.mapVariables.setScale(scale.get());
        this.worldCenter.addListener((observable, oldValue, newValue) -> mapVariables.setWorldCenter(newValue));
        this.mapVariables.setWorldCenter(worldCenter.get());
        this.viewWidth.addListener((observable, oldValue, newValue) -> mapVariables.setViewWidth(newValue.doubleValue()));
        this.mapVariables.setViewWidth(viewWidth.get());
        this.viewHeight.addListener((observable, oldValue, newValue) -> mapVariables.setViewHeight(newValue.doubleValue()));
        this.mapVariables.setViewHeight(viewHeight.get());

        Notifications.REPAINT.subscribe(this::triggerRepaint);
        Notifications.PAN_TO_DATA.subscribe(this::panToData);

        this.selectedItem.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.viewHeight.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.viewWidth.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.worldCenter.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.scale.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.selectionShape.addListener((observable, oldValue, newValue) -> triggerRepaint());
    }

    private void panToData(final Data data) {
        transitionDataVisitor
                .visit(data)
                .ifPresent(e -> panToData(new WorldPanTransition(new Viewport(getWorldCenter().multiply(-1), scale.get()), e)));
    }

    private void panToData(final WorldPanTransition transition) {
        fireTransition(transition);
    }

    public void goToItem() {
        transitionDataVisitor
                .visit(getSelectedItem())
                .ifPresent(e -> panToData(new WorldPanTransition(new Viewport(getWorldCenter().multiply(-1), scale.get()), e)));
    }

    public void openClosest() {
        selectedItem.set(contextMenu.closest().orElse(null));
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

        final List<Airport> col = airportRepository
                .streamSearchByPosition(mouseWorldPosition, selectionDistance(), Integer.MAX_VALUE)
                .filter(Airport::hasControllers)
                .limit(10)
                .collect(Collectors.toList());
        contextMenu.getAirports()
                   .getItems()
                   .setAll(col);

        contextMenu.getPilots()
                   .getItems()
                   .setAll(clientRepository.searchByPosition(mouseWorldPosition, selectionDistance(), 3));
    }

    private double selectionDistance() {
        return 10 / scale.get();
    }

    private ObservableList<PainterExecutor<?>> executors(final UpperInformationRegionRepository upperInformationRegionRepository) {
        return FXCollections.observableArrayList(
                PainterExecutor.of("Background", new BackgroundPainter(mapVariables, Color.valueOf("17130a"))),
                PainterExecutor.ofCollection("World", new WorldPainter(mapVariables, Color.valueOf("0f0c02")), this::world),
                PainterExecutor.ofItem("Date Line", new IdlPainter(mapVariables, Color.valueOf("3b3b3b")), this::internationalDateLine),
                PainterExecutor.ofCollection("Inactive Firs", new InactiveFirPainter(mapVariables), this::flightInformationRegionBoundaries, this::isNotSelected),
                PainterExecutor.ofCollection("Active Uirs", new ActiveUirPainter(mapVariables), upperInformationRegionRepository::list, this::isNotSelected),
                PainterExecutor.ofCollection("Active Firs", new ActiveFirPainter(mapVariables), this::flightInformationRegionBoundaries, this::isNotSelected),
                PainterExecutor.ofCollection("Pilots", new PilotPainter(mapVariables), this::pilots, this::isNotSelected),
                PainterExecutor.ofCollection("Airports", new AirportPainter(mapVariables), this::airports, this::isNotSelected),
                PainterExecutor.ofItem("Selected Item", new SelectedPainter(mapVariables), selectedItem::get),
                PainterExecutor.ofItem("Selection Shape", new SelectionShapePainter(mapVariables), selectionShape::get),
                PainterExecutor.ofItem("Metrics", new FrameMetricsPainter(mapVariables), () -> frameMetrics)
        );
    }

    private boolean isNotSelected(final Data e) {
        return e != selectedItem.get();
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

    public ContextMenuViewModel showingContextMenu() {
        selectionShape.set(
                new Circle2D(mouseViewPosition.get(), selectionDistance())
        );
        return contextMenu;
    }

    public void hideContextMenu() {
        selectionShape.set(null);
    }

    public void onFrameCompleted(final long totalFrameTimeNanos) {
        for (final PainterExecutor<?> painterExecutor : painterExecutors) {
            frameMetrics.getMetric(painterExecutor.getName()).append(painterExecutor.getLastDurationNanos());
        }

        frameMetrics.getMetric("Total").append(totalFrameTimeNanos);
    }

    private class TransitionDataVisitor implements OptionalDataVisitor<Viewport> {
        private Optional<Viewport> defaultTarget(final Point2D position) {
            if (position == null) {
                return Optional.empty();
            }

            return Optional.of(new Viewport(position, 32));
        }

        @Override
        public Optional<Viewport> visit(final Airport airport) {
            return defaultTarget(airport.getPosition());
        }

        @Override
        public Optional<Viewport> visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
            final Rectangle2D boundary = flightInformationRegionBoundary.getPolygon().boundary();

            return fromRect(boundary);
        }

        private Optional<Viewport> fromRect(final Rectangle2D boundary) {
            final double targetScale = mapVariables.scaleForRectFit(boundary.getWidth() * 1.1, boundary.getHeight() * 1.1);

            final double x = boundary.getMinX() + boundary.getWidth() / 2.0;
            final double y = boundary.getMinY() + boundary.getHeight() / 2.0;

            final Point2D targetWorldCenter = new Point2D(x, y);

            return Optional.of(new Viewport(targetWorldCenter, targetScale));
        }

        @Override
        public Optional<Viewport> visit(final Pilot pilot) {
            return defaultTarget(pilot.getPosition());
        }

        @Override
        public Optional<Viewport> visit(final UpperInformationRegion upperInformationRegion) {
            return fromRect(upperInformationRegion.getBounds());
        }
    }

    @lombok.Data
    private static class Viewport {
        private final Point2D worldCenter;
        private final double scale;
    }

    private class WorldPanTransition extends Transition {
        private final Point2D startingWorldCenter;
        private final Point2D targetWorldCenter;

        private final double startingScale;
        private final double targetScale;

        public WorldPanTransition(final Viewport starting, final Viewport target) {
            super(30);
            setCycleDuration(Duration.seconds(1));
            setCycleCount(1);
//            setInterpolator(Interpolator.EASE_BOTH);
            this.targetWorldCenter = target.worldCenter.multiply(-1);
            this.startingWorldCenter = starting.worldCenter.multiply(-1);

            this.targetScale = target.scale;
            this.startingScale = starting.scale + (starting.scale == 1 ? 0.000000001 : 0);
        }

        @Override
        protected void interpolate(final double frac) {
//            worldCenter.set(position(frac));
//            scale.set(scale(frac));
            final double f1 = frac;
            worldCenter.set(position(f1));

            scale.set(scale(frac));
        }

        private double smoothScale(final double x) {
            final double s = startingScale;
            final double t = targetScale;

            final double a = (-Math.sqrt(s * t - s - t + 1) + s - 1) / (s - t);
            final double u = (s - 1) / (a * a);
            final double v = (2 - 2 * s) / a;
            final double w = s;

            final double y = u * x * x + v * x + w;

            return y;
        }

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
