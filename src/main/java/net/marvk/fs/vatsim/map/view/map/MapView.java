package net.marvk.fs.vatsim.map.view.map;

import com.sun.javafx.scene.control.ContextMenuContent;
import de.saxsys.mvvmfx.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailViewModel;
import net.marvk.fs.vatsim.map.view.painter.PainterExecutor;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class MapView implements FxmlView<MapViewModel> {
    private static final double D_SCROLL = 1.5;
    private static final double D_DRAG = 1;
    private static final int MAX_SCALE = 4096;
    private static final int MIN_SCALE = 1;

    @FXML
    private StackPane stackPane;

    private final Canvas canvas;

    private final InputEventHandler inputEventHandler = new InputEventHandler();

    @InjectViewModel
    private MapViewModel viewModel;

    @InjectContext
    private Context context;

    private final Renderer renderer = new Renderer();

    private MapContextMenu contextMenu;

    private ViewTuple<DataDetailView, DataDetailViewModel> detailView;

    private ObjectProperty<Cursor> cursorProperty = new SimpleObjectProperty<>();

    public MapView() {
        this.canvas = new Canvas(100, 100);
        this.canvas.setFocusTraversable(true);
        this.canvas.addEventFilter(MouseEvent.ANY, e -> canvas.requestFocus());

        this.cursorProperty.bind(Bindings.createObjectBinding(
                () -> {
                    if (inputEventHandler.controlDown.get()) {
                        return Cursor.HAND;
                    } else if (inputEventHandler.leftMouseDown.get()) {
                        return Cursor.CROSSHAIR;
                    } else if (inputEventHandler.rightMouseDown.get()) {
                        if (inputEventHandler.rightMouseDrag.get()) {
                            return Cursor.NONE;
                        } else {
                            return Cursor.MOVE;
                        }
                    } else {
                        return Cursor.DEFAULT;
                    }
                },
                inputEventHandler.leftMouseDown,
                inputEventHandler.rightMouseDown,
                inputEventHandler.leftMouseDrag,
                inputEventHandler.rightMouseDrag,
                inputEventHandler.controlDown
        ));

        this.canvas.cursorProperty().bind(cursorProperty);

        final InputStream s = getClass().getResourceAsStream("../fonts/JetBrainsMono-Regular.ttf");
        final Font font = Font.loadFont(s, 12);

        final GraphicsContext c = this.canvas.getGraphicsContext2D();
        c.setFont(font);
    }

    public void initialize() {
        this.contextMenu = new MapContextMenu();

        this.viewModel.viewWidthProperty().bind(this.canvas.widthProperty());
        this.viewModel.viewHeightProperty().bind(this.canvas.heightProperty());

        this.stackPane.getChildren().add(new MapCanvasPane(canvas));
        loadDetailView();

        this.stackPane.getChildren().add(detailViewOverlay());

        this.canvas.setOnScroll(inputEventHandler::onScroll);

        this.canvas.setOnMouseDragged(inputEventHandler::onDrag);
        this.canvas.setOnMousePressed(inputEventHandler::onStart);
        this.canvas.setOnMouseReleased(inputEventHandler::onRelease);
        this.canvas.setOnKeyPressed(inputEventHandler::onKeyPressed);
        this.canvas.setOnKeyReleased(inputEventHandler::onKeyReleased);

        this.canvas.setOnMouseMoved(inputEventHandler::onMove);

        invalidateCanvas();

        viewModel.subscribe("REPAINT", (key, payload) -> invalidateCanvas());
    }

    private void loadDetailView() {
        detailView = FluentViewLoader
                .fxmlView(DataDetailView.class)
                .context(context)
                .load();

        detailView.getViewModel().dataProperty().bindBidirectional(viewModel.selectedItemProperty());
        detailView.getView().visibleProperty().bind(Bindings.createBooleanBinding(
                () -> viewModel.getSelectedItem() != null,
                viewModel.selectedItemProperty()
        ));
    }

    private Parent detailViewOverlay() {
        final var pane = new BorderPane();
        pane.setPickOnBounds(false);
        pane.setRight(detailView.getView());

        return pane;
    }

    private synchronized void invalidateCanvas() {
        renderer.invalidate();
    }

    private class InputEventHandler {
        private double lastX = 0;
        private double lastY = 0;

        private final BooleanProperty leftMouseDown = new SimpleBooleanProperty();
        private final BooleanProperty leftMouseDrag = new SimpleBooleanProperty();
        private final BooleanProperty rightMouseDown = new SimpleBooleanProperty();
        private final BooleanProperty rightMouseDrag = new SimpleBooleanProperty();
        private final BooleanProperty controlDown = new SimpleBooleanProperty();

        public void onStart(final MouseEvent event) {
            contextMenu.hideAndClear();

            if (!leftMouseDown.get() && event.isPrimaryButtonDown()) {
                if (controlDown.get()) {
                    viewModel.openClosest();
                } else {
                    contextMenu.show(event.getScreenX(), event.getScreenY(), viewModel.showingContextMenu());
                }
            }

            leftMouseDown.set(event.isPrimaryButtonDown());
            rightMouseDown.set(event.isSecondaryButtonDown());

            System.out.println("InputEventHandler.onStart");
            status();

            lastX = event.getX();
            lastY = event.getY();
        }

        private void status() {
            System.out.println("leftMouseDown  " + leftMouseDown.get());
            System.out.println("leftMouseDrag  " + leftMouseDrag.get());
            System.out.println("rightMouseDown " + rightMouseDown.get());
            System.out.println("rightMouseDrag " + rightMouseDrag.get());
            System.out.println("controlDown    " + controlDown.get());
            System.out.println();
        }

        public void onDrag(final MouseEvent event) {
            leftMouseDrag.set(event.isPrimaryButtonDown());
            rightMouseDrag.set(event.isSecondaryButtonDown());

            System.out.println("InputEventHandler.onDrag");
            status();
            if (!rightMouseDown.get()) {
                return;
            }

            final double x = event.getX();
            final double y = event.getY();
            final double dx = (x - lastX) * D_DRAG / MapVariables.WORLD_ASPECT_RATIO;
            final double dy = -(y - lastY) * D_DRAG / MapVariables.WORLD_ASPECT_RATIO;

            final double aspect = (
                    2 * MapVariables.WORLD_WIDTH * (1 / viewModel.scaleProperty().get())) /
                    viewModel.viewWidthProperty().get();

            final Point2D worldCenter = viewModel.getWorldCenter();
            viewModel.setWorldCenter(worldCenter.add(dx * aspect, dy * aspect));

            lastX = x;
            lastY = y;
        }

        public void onRelease(final MouseEvent event) {
            leftMouseDown.set(event.isPrimaryButtonDown());
            rightMouseDown.set(event.isSecondaryButtonDown());

            leftMouseDrag.set(event.isPrimaryButtonDown());
            rightMouseDrag.set(event.isSecondaryButtonDown());

            System.out.println("InputEventHandler.onRelease");
            status();
        }

        public void onMove(final MouseEvent event) {
            viewModel.mouseViewPositionProperty().set(new Point2D(event.getX(), event.getY()));

            System.out.println("InputEventHandler.onMove");
            status();
        }

        public void onScroll(final ScrollEvent event) {
            contextMenu.hideAndClear();
            final double delta = event.getDeltaY() > 0 ? D_SCROLL : 1.0 / D_SCROLL;

            final double oldScale = viewModel.scaleProperty().get();
            final double newScale = Math.min(Math.max(oldScale * delta, MIN_SCALE), MAX_SCALE);
            viewModel.scaleProperty().set(newScale);
        }

        public void onKeyPressed(final KeyEvent keyEvent) {
            if (keyEvent.getCode() == KeyCode.CONTROL) {
                controlDown.set(true);
            }
        }

        public void onKeyReleased(final KeyEvent keyEvent) {
            if (keyEvent.getCode() == KeyCode.CONTROL) {
                controlDown.set(false);
            }
        }
    }

    private final class MapContextMenu extends ContextMenu {
        private ContextMenuViewModel contextMenuViewModel = null;

        private final DataVisitor<String> labelVisitor = new DataVisitor<>() {
            @Override
            public String visit(final Airport airport) {
                return airport.getIcao();
            }

            @Override
            public String visit(final FlightInformationRegionBoundary firb) {
                return firb.getIcao() + (firb.isOceanic() ? " Oceanic" : "");
            }

            @Override
            public String visit(final Pilot visitor) {
                return visitor.getCallsign();
            }
        };

        private final List<EventType<MouseEvent>> transparentEventTypes = List.of(
                MouseEvent.MOUSE_ENTERED,
                MouseEvent.MOUSE_ENTERED_TARGET,
                MouseEvent.MOUSE_EXITED,
                MouseEvent.MOUSE_EXITED_TARGET
        );

        public MapContextMenu() {
            setSkin(createDefaultSkin());

            addEventFilter(MouseEvent.ANY, this::onEvent);

            getSkin().getNode().cursorProperty().bind(cursorProperty);
        }

        private void onEvent(final MouseEvent event) {
            if (event.getTarget() instanceof ContextMenuContent) {
                if (!transparentEventTypes.contains(event.getEventType())) {
                    final Point2D inStackPane = stackPane.screenToLocal(event.getScreenX(), event.getScreenY());
                    final Point2D inCanvas = canvas.parentToLocal(inStackPane);

                    final Point2D canvasSceneOffset = canvas.localToScene(0, 0);
                    final MouseEvent e = new MouseEvent(
                            this,
                            canvas,
                            event.getEventType(),
                            inCanvas.getX(),
                            inCanvas.getY() + canvasSceneOffset.getY(),
                            event.getScreenX(),
                            event.getScreenY(),
                            event.getButton(),
                            event.getClickCount(),
                            event.isShiftDown(),
                            event.isControlDown(),
                            event.isAltDown(),
                            event.isMetaDown(),
                            event.isPrimaryButtonDown(),
                            event.isMiddleButtonDown(),
                            event.isSecondaryButtonDown(),
                            event.isBackButtonDown(),
                            event.isForwardButtonDown(),
                            event.isSynthesized(),
                            event.isPopupTrigger(),
                            event.isStillSincePress(),
                            null
                    );
                    canvas.fireEvent(e);
                    event.consume();

                    if (event.getButton() == MouseButton.PRIMARY) {
                        inputEventHandler.leftMouseDown.set(false);
                    }
                }
            }
        }

        @Override
        public void show(final Node anchor, final Side side, final double dx, final double dy) {
            super.show(anchor, side, dx, dy);
        }

        @Override
        public void show(final Node anchor, final double screenX, final double screenY) {
            super.show(anchor, screenX, screenY);
        }

        public void show(final double screenX, final double screenY, final ContextMenuViewModel items) {
            setupItems(items);
            show(stackPane, screenX, screenY);
        }

        @Override
        public void hide() {
            viewModel.hideContextMenu();
            super.hide();
        }

        private void setupItems(final ContextMenuViewModel items) {
            getItems().clear();

            contextMenuViewModel = new ContextMenuViewModel(items);

            boolean addSeparator = false;

            for (int i = 0; i < contextMenuViewModel.getContextMenuItems().size(); i++) {
                final var contextMenuItem = contextMenuViewModel.getContextMenuItems().get(i);
                final var empty = contextMenuItem.getItems().isEmpty();
                if (addSeparator) {
                    getItems().add(new SeparatorMenuItem());
                }
                final MenuItem header = header(contextMenuItem.getLabel());
                getItems().add(header);
                if (empty) {
                    header.setVisible(false);
                } else {
                    addSeparator = true;
                }
                for (final Data item : contextMenuItem.getItems()) {
                    final MenuItem data = item(labelVisitor.visit(item));
                    getItems().add(data);
                    data.setOnAction(e -> {
                        hide();
                        if (inputEventHandler.controlDown.get()) {
                            viewModel.goToItem();
                        }
                    });
                }
            }

            if (getItems().size() <= contextMenuViewModel.getContextMenuItems().size()) {
                getItems().add(header("Nothing here..."));
            }

            final ContextMenuContent cmc = (ContextMenuContent) getSkin().getNode();

            final ObservableList<Node> cmcChildren = cmc.getItemsContainer().getChildren();
            int id = 0;
            for (final Node child : cmcChildren) {
                if (!(child instanceof ContextMenuContent.MenuItemContainer)) {
                    continue;
                }

                final ContextMenuContent.MenuItemContainer node = (ContextMenuContent.MenuItemContainer) child;
                final int finalId = id++;
                node.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        final var item = contextMenuViewModel.getItem(finalId);

                        if (item != null) {
                            viewModel.setSelectedItem(item);
                        }
                    }
                });
            }
        }

        private MenuItem item(final String label) {
            final MenuItem data = new MenuItem(" " + label);
            data.getStyleClass().add("menu-item-data");
            return data;
        }

        private MenuItem header(final String label) {
            final MenuItem header = new MenuItem(label);
            header.getStyleClass().add("menu-item-header");
            return header;
        }

        public void hideAndClear() {
            if (isShowing()) {
                viewModel.setSelectedItem(null);
                hide();
            }
        }
    }

    private final class Renderer {
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Semaphore renderSemaphore = new Semaphore(1);

        private final DoubleProperty lastFrameTimeMillis = new SimpleDoubleProperty();

        private Renderer() {
            start();
        }

        private void start() {
            executor.submit(() -> {
                while (true) {
                    try {
                        renderSemaphore.acquire();
                        log.info("Drawing frame");
                        final long start = System.nanoTime();
                        redraw().get();
                        final double frameTimeMillis = (System.nanoTime() - start) / 1000000.;
                        lastFrameTimeMillis.set(frameTimeMillis);
                        log.info("Frame time was " + frameTimeMillis + "ms");
                    } catch (final InterruptedException | ExecutionException e) {
                        log.error("", e);
                    }
                }
            });
        }

        private RunnableFuture<Void> redraw() {
            final FutureTask<Void> task = new FutureTask<>(() -> {
                for (final PainterExecutor<?> painterExecutor : viewModel.getPainterExecutors()) {
                    painterExecutor.paint(canvas.getGraphicsContext2D());
                    log.info(painterExecutor.getName() + " finished in " + (painterExecutor.getLastDurationNanos() / 1000000.0) + "ms");
                }
            }, null);

            Platform.runLater(task);

            return task;
        }

        public void invalidate() {
            incrementSemaphore();
        }

        private void incrementSemaphore() {
            if (renderSemaphore.availablePermits() == 0) {
                renderSemaphore.release();
            }
        }
    }
}








































