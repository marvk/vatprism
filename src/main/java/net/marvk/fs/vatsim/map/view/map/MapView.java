package net.marvk.fs.vatsim.map.view.map;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.ClientViewModel;
import net.marvk.fs.vatsim.map.data.Point;

import java.util.Arrays;

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

    public MapView() {
        this.canvas = new Canvas(100, 100);
        this.canvas.setFocusTraversable(true);
        this.canvas.addEventFilter(MouseEvent.ANY, e -> canvas.requestFocus());
        this.canvas.widthProperty().addListener((observable, oldValue, newValue) -> {
            onSizeChange();
            redraw();
        });
        this.canvas.heightProperty().addListener((observable, oldValue, newValue) -> {
            onSizeChange();
            redraw();
        });
    }

    public void initialize() {
        this.viewModel.scaleProperty().addListener((observable, oldValue, newValue) -> {
            onScaleChanged();
            redraw();
        });

        this.viewModel.worldCenterProperty().addListener((observable, oldValue, newValue) -> {
            onCenterChanged();
            redraw();
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

            final Point2D oldPoint = viewModel.worldCenterProperty().get();
            viewModel.worldCenterProperty().set(oldPoint.add(dx, dy));
        });

        this.canvas.setOnMouseDragged(mouseEventHandler::onDrag);
        this.canvas.setOnMousePressed(mouseEventHandler::onStart);

        onCenterChanged();
        onScaleChanged();
        onSizeChange();
        redraw();
    }

    private void onCenterChanged() {
        this.worldCenterX = viewModel.worldCenterProperty().get().getX();
        this.worldCenterY = viewModel.worldCenterProperty().get().getY();
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

    private void redraw() {
        final GraphicsContext c = canvas.getGraphicsContext2D();
        drawBackground(c);
        drawWorld(c);
        drawClients(c);
    }

    private void drawWorld(final GraphicsContext c) {
        c.setStroke(Color.valueOf("A5CBC3"));
        for (int i = 0; i < viewModel.worldX().size(); i++) {

            final double[] x = Arrays.stream(viewModel.worldX().get(i)).map(this::toCanvasX).toArray();
            final double[] y = Arrays.stream(viewModel.worldY().get(i)).map(this::toCanvasY).toArray();
            c.strokePolyline(x, y, x.length);
        }
    }

    private void drawBackground(final GraphicsContext c) {
        c.setFill(Color.valueOf("100B00"));
        c.fillRect(0, 0, viewWidth, viewHeight);
    }

    private void drawClients(final GraphicsContext c) {
        final Color greenyellow = Color.valueOf("85cb33");
        for (final ClientViewModel client : viewModel.clients()) {
            final Point point = client.clientStatus().position().get();
            c.getPixelWriter().setColor(
                    (int) toCanvasX(point.getX()),
                    (int) toCanvasY(point.getY()),
                    greenyellow
            );
        }
    }

    private double toCanvasX(final double modelX) {
        final double zeroStart = ((modelX + worldCenterX) * scale + WORLD_HALF_WIDTH);
        final double normalized = zeroStart / WORLD_WIDTH;

        return normalized * viewWidth;
    }

    private double toModelX(final double canvasX) {
        return 0;
    }

    private double toCanvasY(final double modelY) {
        final double aspect = viewWidth / viewHeight;
        final double aspectScale = aspect / WORLD_ASPECT_RATIO;

        final double zeroStart = ((modelY + worldCenterY) * scale * aspectScale + WORLD_HALF_HEIGHT);
        final double normalized = zeroStart / WORLD_HEIGHT;

        return viewHeight - normalized * viewHeight;
    }

    private double toModelY(final double canvasY) {
        return 0;
    }

    private class MouseEventHandler {
        private Double lastX = null;
        private Double lastY = null;

        public void onStart(final MouseEvent event) {
            lastX = event.getX();
            lastY = event.getY();
            redraw();
        }

        public void onDrag(final MouseEvent event) {
            final double x = event.getX();
            final double y = event.getY();
            final double dx = (x - lastX) * D_DRAG / WORLD_ASPECT_RATIO;
            final double dy = -(y - lastY) * D_DRAG / WORLD_ASPECT_RATIO;

            viewModel.worldCenterProperty().set(viewModel.worldCenterProperty().get().add(dx, dy));

            lastX = x;
            lastY = y;
        }
    }
}
