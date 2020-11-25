package net.marvk.fs.vatsim.map.view.map;

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
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.map.data.*;

import java.util.concurrent.*;

@Slf4j
public class MapView implements FxmlView<MapViewModel> {
    private static final double D_SCROLL = 0.25;
    private static final double D_PAN = 1.;
    private static final double D_DRAG = 1;

    @FXML
    private StackPane stackPane;

    private final Canvas canvas;

    private static final double WORLD_WIDTH = 360;
    private static final double WORLD_HALF_WIDTH = 180;
    private static final double WORLD_HEIGHT = 180;
    private static final double WORLD_HALF_HEIGHT = 90;

    private static final double WORLD_ASPECT_RATIO = WORLD_WIDTH / WORLD_HEIGHT;

    private double scale;

    private double viewWidth = 0;
    private double viewHeight = 0;

    private double viewHalfWidth = 0;
    private double viewHalfHeight = 0;

    private double worldCenterX = 0;
    private double worldCenterY = 0;

    private final MouseEventHandler mouseEventHandler = new MouseEventHandler();

    @InjectViewModel
    private MapViewModel viewModel;

    private final double[] xBuf = new double[1000];
    private final double[] yBuf = new double[1000];

    private final Renderer renderer = new Renderer();

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

    public MapView() {
        this.canvas = new Canvas(100, 100);
        this.canvas.setFocusTraversable(true);
        this.canvas.addEventFilter(MouseEvent.ANY, e -> canvas.requestFocus());
        this.canvas.widthProperty().addListener((observable, oldValue, newValue) -> {
            onSizeChange();
            invalidateCanvas();
        });
        this.canvas.heightProperty().addListener((observable, oldValue, newValue) -> {
            onSizeChange();
            invalidateCanvas();
        });

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
        this.viewModel.scaleProperty().addListener((observable, oldValue, newValue) -> {
            onScaleChanged();
            invalidateCanvas();
        });

        this.viewModel.worldCenterProperty().addListener((observable, oldValue, newValue) -> {
            onCenterChanged();
            invalidateCanvas();
        });

        this.stackPane.getChildren().add(new MapCanvasPane(canvas));

        this.canvas.setOnScroll(event -> {
            final double delta = event.getDeltaY() > 0 ? D_SCROLL : -D_SCROLL;

            final double oldScale = viewModel.scaleProperty().get();
            final double newScale = Math.min(Math.max(oldScale + delta, 1), 10);
            viewModel.scaleProperty().set(newScale);
        });

        this.canvas.setOnKeyPressed(event -> {
            event.consume();
            final double dx = switch (event.getCode()) {
                case LEFT -> D_PAN;
                case RIGHT -> -D_PAN;
                default -> 0;
            };
            final double dy = switch (event.getCode()) {
                case UP -> -D_PAN;
                case DOWN -> D_PAN;
                default -> 0;
            };

            final Point2D oldPoint = viewModel.getWorldCenter();
            viewModel.setWorldCenter(oldPoint.add(dx, dy));
        });

        this.canvas.setOnMouseDragged(mouseEventHandler::onDrag);
        this.canvas.setOnMousePressed(mouseEventHandler::onStart);
        this.canvas.setOnMouseReleased(mouseEventHandler::onRelease);

        onCenterChanged();
        onScaleChanged();
        onSizeChange();
        invalidateCanvas();
    }

    private void onCenterChanged() {
        this.worldCenterX = viewModel.getWorldCenter().getX();
        this.worldCenterY = viewModel.getWorldCenter().getY();
    }

    private void onScaleChanged() {
        this.scale = viewModel.scaleProperty().get();
    }

    private void onSizeChange() {
        this.viewWidth = canvas.getWidth();
        this.viewHeight = canvas.getHeight();
        this.viewHalfWidth = canvas.getWidth() / 2;
        this.viewHalfHeight = canvas.getHeight() / 2;
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
            final GraphicsContext c = canvas.getGraphicsContext2D();
            time(() -> drawBackground(c), "Background finished in ");
            time(() -> drawWorld(c), "World finished in ");
            time(() -> drawIdl(c), "IDL finished in ");
            time(() -> drawAirports(c), "Airports finished in ");
            time(() -> drawFirs(c), "Firs finished in ");
            time(() -> drawClients(c), "Clients finished in ");
        }, null);

        Platform.runLater(task);

        return task;
    }

    private void drawIdl(final GraphicsContext c) {
        final ObservableList<Point2D> points = viewModel.internationalDateLine().points();

        c.setLineWidth(1);
        c.setLineDashes(1, 10);
        for (int i = 0; i < points.size(); i++) {
            final Point2D point2D = points.get(i);

            xBuf[i] = toCanvasX(point2D.getX());
            yBuf[i] = toCanvasY(point2D.getY());
        }
        c.strokePolyline(xBuf, yBuf, points.size());
        c.setLineDashes(null);
    }

    private void drawAirports(final GraphicsContext c) {
        final Color color = Color.DARKGRAY;
        for (final AirportViewModel airport : viewModel.airports()) {
            final Point2D point = airport.positionProperty().get();

            c.getPixelWriter().setColor(
                    (int) toCanvasX(point.getX()),
                    (int) toCanvasY(point.getY()),
                    color
            );
        }
    }

    private int y = 0;

    private void drawFirs(final GraphicsContext c) {
        final Color firColor = Color.valueOf("3B341F");
        final Color activeFirColor = Color.valueOf("#3B341F");

        final ObservableList<FlightInformationRegionBoundaryViewModel> boundaries =
                viewModel.flightInformationRegionBoundaries();

        for (int i = 0; i < boundaries.size(); i++) {
            c.setLineWidth(0.5);
            c.setStroke(firColor);

            final FlightInformationRegionBoundaryViewModel fir = boundaries.get(i);
            if (fir.extensionProperty().get()) {
                continue;
            }
            final Polygon polygon = fir.getPolygon();

//            if (i == y) {
//                c.setLineWidth(3);
//                final Point2D min = polygon.getMin();
//                final Point2D max = polygon.getMax();
//
//                System.out.println(fir.icaoProperty().get());
//                System.out.println("min = " + min);
//                System.out.println("max = " + max);
//
//                final double x1 = toCanvasX(min.getX());
//                final double y1 = toCanvasY(max.getY());
//                final double x2 = toCanvasX(max.getX());
//                final double y2 = toCanvasY(min.getY());
//
//                c.setStroke(Color.RED);
//                c.strokeRect(
//                        x1,
//                        y1,
//                        x2 - x1,
//                        y2 - y1
//                );
//
//                c.setStroke(Color.CYAN);
//            }

            drawPolygons(c, polygon, false);
        }
    }

    private void drawPolygons(final GraphicsContext c, final Polygon polygon, final boolean polyline) {
        if (toCanvasX(polygon.getMin().getX()) < 0) {
            drawPolygon(c, polygon, 360, polyline);
        }

        if (toCanvasX(polygon.getMax().getX()) > viewWidth) {
            drawPolygon(c, polygon, -360, polyline);
        }

        drawPolygon(c, polygon, 0, polyline);
    }

    private void drawPolygon(final GraphicsContext c, final Polygon points, final double offsetX, final boolean polyline) {
        for (int i = 0; i < points.size(); i++) {
            xBuf[i] = toCanvasX(points.getPointsX()[i] + offsetX);
            yBuf[i] = toCanvasY(points.getPointsY()[i]);
        }

        if (polyline) {
            c.strokePolyline(xBuf, yBuf, points.size());
        } else {
            c.strokePolygon(xBuf, yBuf, points.size());
        }
    }

    private void drawWorld(final GraphicsContext c) {
        c.setLineWidth(1);
        c.setStroke(Color.valueOf("A5CBC3"));

        for (final Polygon polygon : viewModel.world()) {
            drawPolygons(c, polygon, true);
        }
    }

    private void drawBackground(final GraphicsContext c) {
        c.setFill(Color.valueOf("100B00"));
        c.fillRect(0, 0, viewWidth, viewHeight);
    }

    private void drawClients(final GraphicsContext c) {
        final Color color = Color.valueOf("85cb33");
        for (final ClientViewModel client : viewModel.clients()) {
            if (client.rawClientTypeProperty().get() != RawClientType.PILOT) {
                continue;
            }

            final Point2D point = client.clientStatus().position().get();
            final int x = (int) toCanvasX(point.getX());
            final int y = (int) toCanvasY(point.getY());
            c.getPixelWriter().setColor(x - 1, y + 1, color);
            c.getPixelWriter().setColor(x - 1, y, color);
            c.getPixelWriter().setColor(x - 1, y - 1, color);
            c.getPixelWriter().setColor(x, y + 1, color);
            c.getPixelWriter().setColor(x, y - 1, color);
            c.getPixelWriter().setColor(x + 1, y + 1, color);
            c.getPixelWriter().setColor(x + 1, y, color);
            c.getPixelWriter().setColor(x + 1, y - 1, color);
        }
    }

    private double toCanvasX(final double modelX) {
        final double zeroStart = ((modelX + worldCenterX) * scale + WORLD_HALF_WIDTH);
        final double normalized = zeroStart / WORLD_WIDTH;

        return normalized * viewWidth;
    }

    private double toModelX(final double canvasX) {
        return (((canvasX / viewWidth) * WORLD_WIDTH - WORLD_HALF_HEIGHT) / scale) - worldCenterX;
    }

    private double toCanvasY(final double modelY) {
        final double zeroStart = ((modelY + worldCenterY) * scale * aspectScaleY() + WORLD_HALF_HEIGHT);
        final double normalized = zeroStart / WORLD_HEIGHT;

        return viewHeight - normalized * viewHeight;
    }

    private double toModelY(final double canvasY) {
        return ((((1 - canvasY / viewHeight) * WORLD_HEIGHT - WORLD_HALF_HEIGHT) / scale) / aspectScaleY()) - worldCenterY;
    }

    private double aspectScaleY() {
        return viewWidth / viewHeight / WORLD_ASPECT_RATIO;
    }

    private class MouseEventHandler {
        private double lastX = 0;
        private double lastY = 0;

        private final BooleanProperty leftMouseDown = new SimpleBooleanProperty();
        private final BooleanProperty rightMouseDown = new SimpleBooleanProperty();

        public void onStart(final MouseEvent event) {
            leftMouseDown.set(event.isPrimaryButtonDown());
            rightMouseDown.set(event.isSecondaryButtonDown());

            lastX = event.getX();
            lastY = event.getY();
            invalidateCanvas();

            if (event.isPrimaryButtonDown()) {
                y++;
            }
        }

        public void onDrag(final MouseEvent event) {
            if (!rightMouseDown.get()) {
                return;
            }

            final double x = event.getX();
            final double y = event.getY();
            final double dx = (x - lastX) * D_DRAG / WORLD_ASPECT_RATIO;
            final double dy = -(y - lastY) * D_DRAG / WORLD_ASPECT_RATIO;

            final double aspect = (2 * WORLD_WIDTH * (1 / scale)) / viewWidth;

            final Point2D worldCenter = viewModel.getWorldCenter();
            viewModel.setWorldCenter(worldCenter.add(dx * aspect, dy * aspect));

            lastX = x;
            lastY = y;
        }

        public void onRelease(final MouseEvent event) {
            leftMouseDown.set(event.isPrimaryButtonDown());
            rightMouseDown.set(event.isSecondaryButtonDown());
        }
    }
}
