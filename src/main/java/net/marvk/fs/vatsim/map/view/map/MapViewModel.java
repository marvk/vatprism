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
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.SettingsScope;
import net.marvk.fs.vatsim.map.view.StatusScope;
import net.marvk.fs.vatsim.map.view.painter.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class MapViewModel implements ViewModel {
    private static final int SELECTION_DISTANCE = 8;
    private final DoubleProperty scale = new SimpleDoubleProperty(1);
    private final ReadOnlyObjectWrapper<Point2D> worldCenter = new ReadOnlyObjectWrapper<>(new Point2D(0, 0));
    private final DoubleProperty viewWidth = new SimpleDoubleProperty();
    private final DoubleProperty viewHeight = new SimpleDoubleProperty();

    private final ObjectProperty<Point2D> mouseViewPosition = new SimpleObjectProperty<>(new Point2D(0, 0));
    private final ObjectProperty<Point2D> mouseWorldPosition = new SimpleObjectProperty<>(new Point2D(0, 0));

    private final ReadOnlyDoubleWrapper scrollSpeed = new ReadOnlyDoubleWrapper();

    private final ReadOnlyDoubleWrapper fontSize = new ReadOnlyDoubleWrapper();

    private final ClientRepository clientRepository;
    private final AirportRepository airportRepository;
    private final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository;

    private final InternationalDateLineRepository internationalDateLineRepository;
    private final UpperInformationRegionRepository upperInformationRegionRepository;

    private final List<Polygon> world;
    private final List<Polygon> lakes;

    private final MapVariables mapVariables = new MapVariables();

    private final ContextMenuViewModel contextMenu = new ContextMenuViewModel();

    private final ObjectProperty<Data> selectedItem = new SimpleObjectProperty<>();

    private final ObjectProperty<Object> selectionShape = new SimpleObjectProperty<>();
    private final ObjectProperty<DistanceMeasure> distanceMeasureCanvas = new SimpleObjectProperty<>();
    private final ObjectProperty<DistanceMeasure> distanceMeasureWorld = new SimpleObjectProperty<>();
    private final Preferences preferences;
    private final FilterRepository filterRepository;

    private ObservableList<PainterExecutor<?>> painterExecutors;

    private FrameMetrics frameMetrics;

    @InjectScope
    private StatusScope statusScope;

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
            final Preferences preferences,
            final FilterRepository filterRepository,
            @Named("world") final PolygonRepository world,
            @Named("lakes") final PolygonRepository lakes
    ) {
        this.clientRepository = clientRepository;
        this.airportRepository = airportRepository;
        this.flightInformationRegionBoundaryRepository = flightInformationRegionBoundaryRepository;
        this.internationalDateLineRepository = internationalDateLineRepository;
        this.upperInformationRegionRepository = upperInformationRegionRepository;

        this.preferences = preferences;
        this.filterRepository = filterRepository;

        this.scrollSpeed.bind(preferences.doubleProperty("general.scroll_speed"));

        this.world = world.list();
        this.lakes = lakes.list();

        this.mouseViewPosition.addListener((observable, oldValue, newValue) -> recalculateMouseWorldPosition());

        this.scale.addListener((observable, oldValue, newValue) -> mapVariables.setScale(newValue.doubleValue()));
        this.mapVariables.setScale(scale.get());
        this.worldCenter.addListener((observable, oldValue, newValue) -> mapVariables.setWorldCenter(newValue));
        this.mapVariables.setWorldCenter(worldCenter.get());
        this.viewWidth.addListener((observable, oldValue, newValue) -> mapVariables.setViewWidth(newValue.doubleValue()));
        this.mapVariables.setViewWidth(viewWidth.get());
        this.viewHeight.addListener((observable, oldValue, newValue) -> mapVariables.setViewHeight(newValue.doubleValue()));
        this.mapVariables.setViewHeight(viewHeight.get());

        this.selectedItem.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.viewHeight.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.viewWidth.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.worldCenter.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.scale.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.selectionShape.addListener((observable, oldValue, newValue) -> triggerRepaint());
        this.distanceMeasureCanvas.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                distanceMeasureWorld.set(null);
            } else {
                distanceMeasureWorld.set(new DistanceMeasure(
                        mapVariables.toWorld(newValue.getFrom()),
                        mapVariables.toWorld(newValue.getTo()),
                        newValue.isReleased()
                ));
            }
        });
        this.distanceMeasureWorld.addListener((observable, oldValue, newValue) -> triggerRepaint());

        this.fontSize.bind(preferences.integerProperty("general.map_font_size"));
    }

    public void recalculateMouseWorldPosition() {
        mouseWorldPosition.set(mapVariables.toWorld(mouseViewPosition.get()));
    }

    public void initialize() {
        this.painterExecutors = executors(upperInformationRegionRepository);

        final ArrayList<String> names = painterExecutors
                .stream()
                .map(PainterExecutor::getName)
                .collect(Collectors.toCollection(ArrayList::new));
        names.add(0, "Total");

        this.frameMetrics = new FrameMetrics(names, 250);

//        Bindings.bindContent(statusScope.highlightedFirs(), contextMenu.getFirbs().getItems());
        statusScope.mouseViewPositionProperty().bind(mouseViewPosition);
        statusScope.mouseWorldPositionProperty().bind(mouseWorldPosition);

        Bindings.bindContent(settingsScope.getPainters(), painterExecutors);

        Notifications.REPAINT.subscribe(this::triggerRepaint);
        Notifications.PAN_TO_DATA.subscribe(this::panToData);
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
        setContextMenuItems();
        selectedItem.set(contextMenu.closest(getMouseWorldPosition()).orElse(null));
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

    public void setContextMenuItems() {
        new ContextMenuSetter().set();
    }

    private double selectionDistance() {
        return SELECTION_DISTANCE / scale.get();
    }

    private ObservableList<PainterExecutor<?>> executors(final UpperInformationRegionRepository upperInformationRegionRepository) {
        return FXCollections.observableArrayList(
                PainterExecutor.of("Ocean", "Background", new BackgroundPainter(mapVariables, Color.valueOf("291e0f"))),
                PainterExecutor.ofCollection("Landmass", "World", new WorldPainter(mapVariables, Color.valueOf("1a130a")), this::world),
                PainterExecutor.ofCollection(null, "Lakes", new WorldPainter(mapVariables, Color.valueOf("291e0f")), this::lakes),
                PainterExecutor.ofItem("International Date Line", "Date Line", new IdlPainter(mapVariables, Color.valueOf("3b3b3b")), this::internationalDateLine),
                PainterExecutor.of(null, "Scale", new ScalePainter(mapVariables)),
                PainterExecutor.ofCollection("Inactive Flight Information Regions", "Inactive Firs", new InactiveFirbPainter(mapVariables), this::flightInformationRegionBoundaries, this::isNotSelected),
                PainterExecutor.ofCollection("Inactive Upper Information Regions", "Inactive Uirs", new InactiveUirPainter(mapVariables), upperInformationRegionRepository::list, this::isNotSelected),
                PainterExecutor.ofCollection("Active Upper Information Regions", "Active Uirs", new ActiveUirPainter(mapVariables), upperInformationRegionRepository::list, this::isNotSelected),
                PainterExecutor.ofCollection("Active Flight Information Regions", "Active Firs", new ActiveFirbPainter(mapVariables), this::flightInformationRegionBoundaries, this::isNotSelected),
                PainterExecutor.ofItem("Flight Tracks", "Connections", new ConnectionsPainter(mapVariables), this.selectedItemProperty()::get),
                PainterExecutor.ofCollection("Flights", "Pilots", new PilotPainter(mapVariables), this::pilots, this::isNotSelected),
                PainterExecutor.ofCollection(null, "Filters", new FilterPainter(mapVariables, filterRepository.list()), this::pilots, this::isNotSelected),
                PainterExecutor.ofCollection(null, "Airports", new AirportPainter(mapVariables), this::airports, this::isNotSelected),
                PainterExecutor.ofCollection(null, "Search Items", new SelectedPainter(mapVariables, Color.DEEPSKYBLUE, true), statusScope::getSearchedData, this::isNotSelected),
                PainterExecutor.ofItem(null, "Selected Item", new SelectedPainter(mapVariables), selectedItem::get),
                PainterExecutor.ofItem(null, "Selection Shape", new SelectionShapePainter(mapVariables), selectionShape::get),
                PainterExecutor.ofItem(null, "Distance Measure", new DistanceMeasurePainter(mapVariables), distanceMeasureWorld::get),
                PainterExecutor.ofItem(null, "Metrics", new FrameMetricsPainter(mapVariables), () -> frameMetrics)
        );
    }

    private boolean isNotSelected(final Data e) {
        return e != selectedItem.get();
    }

    private void triggerRepaint() {
        publish("REPAINT");
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

    public List<Polygon> lakes() {
        return lakes;
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

    public double getScrollSpeed() {
        return scrollSpeed.get();
    }

    public ReadOnlyDoubleProperty scrollSpeedProperty() {
        return scrollSpeed.getReadOnlyProperty();
    }

    public double getFontSize() {
        return fontSize.get();
    }

    public ReadOnlyDoubleProperty fontSizeProperty() {
        return fontSize.getReadOnlyProperty();
    }

    public void setDistanceMeasureCanvas(final DistanceMeasure distanceMeasureCanvas) {
        this.distanceMeasureCanvas.set(distanceMeasureCanvas);
    }

    public ContextMenuViewModel showingContextMenu() {
        setContextMenuItems();
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

        painterMetricsSnapshot().forEach(this::logCounter);
    }

    private void logCounter(final PainterMetric.Counter counter) {
        log.trace("%d calls to %s".formatted(counter.getCount(), counter.getName()));
    }

    private PainterMetric painterMetricsSnapshot() {
        final List<PainterMetric> painterMetrics = painterExecutors.stream()
                                                                   .map(PainterExecutor::getLastPainterMetric)
                                                                   .collect(Collectors.toList());

        return PainterMetric.ofMetrics(painterMetrics);
    }

    private class ContextMenuSetter {
        private final Predicate tautology = e -> true;
        private final Predicate contradiction = e -> false;

        public void set() {
            setFirbs();
            setUirs();
            setAirports();
            setPilots();
        }

        private boolean isProperty(final String s) {
            return preferences.booleanProperty(s).get();
        }

        private Predicate<FlightInformationRegionBoundary> firbPredicate() {
            final boolean showAll = isProperty("context_menu.show_all_firs");

            if (showAll) {
                //noinspection unchecked
                return tautology;
            }

            final boolean paintActiveFirs = isProperty("active_firs.enabled");
            final boolean paintInactiveFirs = isProperty("inactive_firs.enabled");

            if (paintActiveFirs && paintInactiveFirs) {
                //noinspection unchecked
                return tautology;
            }

            if (paintActiveFirs) {
                return FlightInformationRegionBoundary::hasFirControllers;
            }

            if (paintInactiveFirs) {
                return e -> !e.hasFirControllers();
            }

            //noinspection unchecked
            return contradiction;
        }

        private Predicate<UpperInformationRegion> uirPredicate() {
            final boolean showAll = isProperty("context_menu.show_all_uirs");

            if (showAll) {
                //noinspection unchecked
                return tautology;
            }

            final boolean paintActiveUirs = isProperty("active_uirs.enabled");
            final boolean paintInactiveUirs = isProperty("inactive_uirs.enabled");

            if (paintActiveUirs && paintInactiveUirs) {
                //noinspection unchecked
                return tautology;
            }

            if (paintActiveUirs) {
                return uir -> !uir.getControllers().isEmpty();
            }

            if (paintInactiveUirs) {
                return uir -> uir.getControllers().isEmpty();
            }

            //noinspection unchecked
            return contradiction;
        }

        private Predicate<Airport> airportPredicate() {
            final boolean showAll = isProperty("context_menu.show_all_airports");

            if (showAll) {
                //noinspection unchecked
                return tautology;
            }

            final boolean paintAirports = isProperty("airports.enabled");

            if (paintAirports) {
                final boolean paintUncontrolledAirports = isProperty("airports.paint_uncontrolled_airports");
                final boolean paintUncontrolledAirportsWithArrivalsOrDepartures = isProperty("airports.paint_uncontrolled_airports_with_arrivals_or_departures");

                if (paintUncontrolledAirports) {
                    //noinspection unchecked
                    return tautology;
                }

                if (paintUncontrolledAirportsWithArrivalsOrDepartures) {
                    return airport -> airport.hasControllers() || airport.hasArrivals() || airport.hasDepartures();
                }

                return Airport::hasControllers;
            }

            //noinspection unchecked
            return contradiction;
        }

        private Predicate<Pilot> pilotPredicate() {
            final boolean showAll = isProperty("context_menu.show_all_pilots");

            if (showAll) {
                //noinspection unchecked
                return tautology;
            }

            final boolean paintPilots = isProperty("pilots.enabled");

            if (paintPilots) {
                final boolean paintPilotsOnGround = isProperty("pilots.pilots_on_ground");

                if (paintPilotsOnGround) {
                    //noinspection unchecked
                    return tautology;
                }

                return pilot -> !pilot.getEta().is(Eta.Status.GROUND);
            }

            //noinspection unchecked
            return contradiction;
        }

        private void setFirbs() {
            final var firbs = firbStream()
                    .filter(firbPredicate())
                    .collect(Collectors.toList());

            contextMenu.getFirbs().getItems().setAll(firbs);
        }

        private Stream<FlightInformationRegionBoundary> firbStream() {
            return flightInformationRegionBoundaryRepository
                    .streamAllByPosition(mouseWorldPosition.get(), selectionDistance());
        }

        private void setUirs() {
            final var uirs = firbStream()
                    .map(FlightInformationRegionBoundary::getUpperInformationRegions)
                    .flatMap(Collection::stream)
                    .distinct()
                    .filter(uirPredicate())
                    .collect(Collectors.toList());

            contextMenu.getUirs().getItems().setAll(uirs);
        }

        private void setAirports() {
            final var airports = airportRepository
                    .streamSearchByPosition(mouseWorldPosition.get(), selectionDistance(), Integer.MAX_VALUE)
                    .filter(airportPredicate())
                    .collect(Collectors.toList());

            contextMenu.getAirports().getItems().setAll(airports);
        }

        private void setPilots() {
            final var pilots = clientRepository
                    .streamSearchByPosition(mouseWorldPosition.get(), selectionDistance(), Integer.MAX_VALUE)
                    .filter(pilotPredicate())
                    .collect(Collectors.toList());

            contextMenu.getPilots().getItems().setAll(pilots);
        }
    }

    private class TransitionDataVisitor implements OptionalDataVisitor<Viewport> {
        private Optional<Viewport> defaultTarget(final Point2D position) {
            if (position == null) {
                return Optional.empty();
            }

            return Optional.of(new Viewport(position, 32));
        }

        @Override
        public Optional<Viewport> visit(final FlightPlan flightPlan) {
            return visit(flightPlan.getPilot());
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

        @Override
        public Optional<Viewport> visit(final Pilot pilot) {
            return defaultTarget(pilot.getPosition());
        }

        @Override
        public Optional<Viewport> visit(final UpperInformationRegion upperInformationRegion) {
            return fromRect(upperInformationRegion.getBounds());
        }

        private Optional<Viewport> fromRect(final Rectangle2D boundary) {
            final double targetScale = mapVariables.scaleForRectFit(boundary.getWidth() * 1.1, boundary.getHeight() * 1.1);

            final double x = boundary.getMinX() + boundary.getWidth() / 2.0;
            final double y = boundary.getMinY() + boundary.getHeight() / 2.0;

            final Point2D targetWorldCenter = new Point2D(x, y);

            return Optional.of(new Viewport(targetWorldCenter, targetScale));
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

            this.targetWorldCenter = target.worldCenter.multiply(-1);
            this.startingWorldCenter = starting.worldCenter.multiply(-1);

            this.targetScale = target.scale;
            this.startingScale = starting.scale + (Double.compare(starting.scale, 1) == 0 ? 0.000000001 : 0);
        }

        @Override
        protected void interpolate(final double frac) {
            worldCenter.set(position(frac));

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
