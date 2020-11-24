package net.marvk.fs.vatsim.map.view.map;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

public class MapCanvasPane extends Pane {
    private final Canvas canvas;

    public MapCanvasPane(final Canvas canvas) {
        this.canvas = canvas;
        getChildren().add(canvas);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        final double x = snappedLeftInset();
        final double y = snappedTopInset();
        final double w = snapSizeX(getWidth()) - x - snappedRightInset();
        final double h = snapSizeY(getHeight()) - y - snappedBottomInset();
        canvas.setLayoutX(x);
        canvas.setLayoutY(y);
        canvas.setWidth(w);
        canvas.setHeight(h);
    }
}
