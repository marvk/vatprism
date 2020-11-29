package net.marvk.fs.vatsim.map.view.map;

import com.sun.javafx.scene.control.ContextMenuContent;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundaryViewModel;
import net.marvk.fs.vatsim.map.view.painter.PainterExecutor;

import java.util.concurrent.*;

@Slf4j
public class MapView implements FxmlView<MapViewModel> {
    private static final double D_SCROLL = 0.25;
    private static final double D_DRAG = 1;

    @FXML
    private StackPane stackPane;

    private final Canvas canvas;

    private final MouseEventHandler mouseEventHandler = new MouseEventHandler();

    @InjectViewModel
    private MapViewModel viewModel;

    private final Renderer renderer = new Renderer();

    private RightClickMenu rightClickMenu;

    @InjectContext
    private Context context;

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
    }

    public void initialize() {
        this.rightClickMenu = new RightClickMenu();

        this.viewModel.viewWidthProperty().bind(this.canvas.widthProperty());
        this.viewModel.viewHeightProperty().bind(this.canvas.heightProperty());

        this.viewModel.scaleProperty().addListener((observable, oldValue, newValue) -> invalidateCanvas());
        this.viewModel.worldCenterProperty().addListener((observable, oldValue, newValue) -> invalidateCanvas());
        this.viewModel.viewWidthProperty().addListener((observable, oldValue, newValue) -> invalidateCanvas());
        this.viewModel.viewHeightProperty().addListener((observable, oldValue, newValue) -> invalidateCanvas());

//        this.viewModel.mouseViewPositionProperty().addListener((observable, oldValue, newValue) -> invalidateCanvas());

        this.viewModel.selectedFir()
                      .addListener((ListChangeListener<FlightInformationRegionBoundaryViewModel>) c -> invalidateCanvas());

        this.stackPane.getChildren().add(new MapCanvasPane(canvas));

        this.canvas.setOnScroll(event -> {
            final double delta = event.getDeltaY() > 0 ? D_SCROLL : -D_SCROLL;

            final double oldScale = viewModel.scaleProperty().get();
            final double newScale = Math.min(Math.max(oldScale + delta, 1), 10);
            viewModel.scaleProperty().set(newScale);
        });

        this.canvas.setOnMouseDragged(mouseEventHandler::onDrag);
        this.canvas.setOnMousePressed(mouseEventHandler::onStart);
        this.canvas.setOnMouseReleased(mouseEventHandler::onRelease);

        this.canvas.setOnMouseMoved(mouseEventHandler::onMove);

        invalidateCanvas();
    }

    private synchronized void invalidateCanvas() {
        renderer.invalidate();
    }

    private void time(final Runnable runnable, final String info) {
        final long start = System.nanoTime();
        runnable.run();
        log.info(info + ((System.nanoTime() - start) / 1000000.0) + "ms");
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

    private class MouseEventHandler {
        private double lastX = 0;
        private double lastY = 0;

        private final BooleanProperty leftMouseDown = new SimpleBooleanProperty();
        private final BooleanProperty rightMouseDown = new SimpleBooleanProperty();

        private boolean moved = false;

        public void onStart(final MouseEvent event) {
            leftMouseDown.set(event.isPrimaryButtonDown());
            rightMouseDown.set(event.isSecondaryButtonDown());

            lastX = event.getX();
            lastY = event.getY();
            invalidateCanvas();

            rightClickMenu.hide();
        }

        public void onDrag(final MouseEvent event) {
            if (!rightMouseDown.get()) {
                return;
            }

            moved = true;

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

            if (rightMouseDown.get() && !event.isSecondaryButtonDown()) {
                if (mouseEventHandler.moved) {
                    moved = false;
                } else {
                    rightClickMenu.show(canvas, event.getScreenX(), event.getScreenY());
                }
            }

            rightMouseDown.set(event.isSecondaryButtonDown());
        }

        public void onMove(final MouseEvent event) {
            viewModel.mouseViewPositionProperty().set(new Point2D(event.getX(), event.getY()));
        }
    }

    private final class RightClickMenu extends ContextMenu {
        public RightClickMenu() {
            setSkin(createDefaultSkin());
        }

        @Override
        public void show(final Node anchor, final Side side, final double dx, final double dy) {
            super.show(anchor, side, dx, dy);
        }

        @Override
        public void show(final Node anchor, final double screenX, final double screenY) {
            super.show(anchor, screenX, screenY);
            setupItems();
        }

        @Override
        public void hide() {
            viewModel.setSelectedFir(null);
            super.hide();
        }

        private void setupItems() {
            getItems().clear();
            for (final FlightInformationRegionBoundaryViewModel h : viewModel.highlightedBoundaries()) {
                getItems().add(new MenuItem(h.icaoProperty().get()));
            }

            final ContextMenuContent cmc = (ContextMenuContent) getSkin().getNode();

            final ObservableList<Node> cmcChildren = cmc.getItemsContainer().getChildren();
            for (int i = 0; i < cmcChildren.size(); i++) {
                final ContextMenuContent.MenuItemContainer node = (ContextMenuContent.MenuItemContainer) cmcChildren.get(i);
                final int finalI = i;
                node.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        viewModel.setSelectedFir(viewModel.highlightedBoundaries().get(finalI));
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








































