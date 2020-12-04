package net.marvk.fs.vatsim.map.view.map;

import com.sun.javafx.scene.control.ContextMenuContent;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.painter.PainterExecutor;

import java.io.InputStream;
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

    private final MouseEventHandler mouseEventHandler = new MouseEventHandler();

    @InjectViewModel
    private MapViewModel viewModel;

    private final Renderer renderer = new Renderer();

    private MapContextMenu contextMenu;

    public MapView() {
        this.canvas = new Canvas(100, 100);
        this.canvas.setFocusTraversable(true);
        this.canvas.addEventFilter(MouseEvent.ANY, e -> canvas.requestFocus());

        this.canvas.cursorProperty().bind(Bindings.createObjectBinding(
                () -> {
                    if (mouseEventHandler.leftMouseDown.get()) {
                        return Cursor.CROSSHAIR;
                    } else if (mouseEventHandler.rightMouseDown.get()) {
                        return Cursor.MOVE;
                    } else {
                        return Cursor.DEFAULT;
                    }
                },
                mouseEventHandler.leftMouseDown,
                mouseEventHandler.rightMouseDown
        ));

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

        this.canvas.setOnScroll(mouseEventHandler::onScroll);

        this.canvas.setOnMouseDragged(mouseEventHandler::onDrag);
        this.canvas.setOnMousePressed(mouseEventHandler::onStart);
        this.canvas.setOnMouseReleased(mouseEventHandler::onRelease);

        this.canvas.setOnMouseMoved(mouseEventHandler::onMove);

        invalidateCanvas();

        viewModel.subscribe("REPAINT", (key, payload) -> invalidateCanvas());
    }

    private synchronized void invalidateCanvas() {
        renderer.invalidate();
    }

    private class MouseEventHandler {
        private double lastX = 0;
        private double lastY = 0;

        private final BooleanProperty leftMouseDown = new SimpleBooleanProperty();
        private final BooleanProperty rightMouseDown = new SimpleBooleanProperty();

        public void onStart(final MouseEvent event) {
            contextMenu.hide();
            System.out.println("leftMouseDown.get() = " + leftMouseDown.get());
            System.out.println("event.isPrimaryButtonDown() = " + event.isPrimaryButtonDown());
            if (!leftMouseDown.get() && event.isPrimaryButtonDown()) {
                System.out.println("show");
                contextMenu.show(stackPane, event.getScreenX(), event.getScreenY());
            }

            leftMouseDown.set(event.isPrimaryButtonDown());
            rightMouseDown.set(event.isSecondaryButtonDown());

            lastX = event.getX();
            lastY = event.getY();
        }

        public void onDrag(final MouseEvent event) {
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
        }

        public void onMove(final MouseEvent event) {
            viewModel.mouseViewPositionProperty().set(new Point2D(event.getX(), event.getY()));
        }

        public void onScroll(final ScrollEvent event) {
            contextMenu.hide();
            final double delta = event.getDeltaY() > 0 ? D_SCROLL : 1.0 / D_SCROLL;

            final double oldScale = viewModel.scaleProperty().get();
            final double newScale = Math.min(Math.max(oldScale * delta, MIN_SCALE), MAX_SCALE);
            viewModel.scaleProperty().set(newScale);
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
            public String visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
                return flightInformationRegionBoundary.getIcao();
            }

            @Override
            public String visit(final Pilot visitor) {
                return visitor.getCallsign();
            }
        };

        public MapContextMenu() {
            setSkin(createDefaultSkin());
        }

        @Override
        public void show(final Node anchor, final Side side, final double dx, final double dy) {
            super.show(anchor, side, dx, dy);
        }

        @Override
        public void show(final Node anchor, final double screenX, final double screenY) {
            setupItems();
            super.show(anchor, screenX, screenY);
        }

        @Override
        public void hide() {
            viewModel.setSelectedItem(null);
            super.hide();
        }

        private void setupItems() {
            getItems().clear();

            contextMenuViewModel = new ContextMenuViewModel(viewModel.getContextMenu());

            for (int i = 0; i < contextMenuViewModel.getContextMenuItems().size(); i++) {
                final var contextMenuItem = contextMenuViewModel.getContextMenuItems().get(i);
                final var empty = contextMenuItem.getItems().isEmpty();
                if (i > 0 && !empty) {
                    getItems().add(new SeparatorMenuItem());
                }
                final MenuItem e = new MenuItem(contextMenuItem.getLabel());
                e.getStyleClass().add("menu-item-header");
                getItems().add(e);
                if (empty) {
                    e.setVisible(false);
                }
                for (final Data item : contextMenuItem.getItems()) {
                    getItems().add(new MenuItem(labelVisitor.visit(item)));
                }
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

                        viewModel.setSelectedItem(item);
                    }
                });
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








































