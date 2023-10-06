package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class ScalePainter extends MapPainter<Void> {
    private static final int MAX_WIDTH = 200;
    private static final int EDGE_OFFSET = 20;
    private static final int LIMIT_LENGTH = 5;

    @Parameter(name = "Color")
    private Color color = Color.web("3b3526");

    public ScalePainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final GraphicsContext context, final Void unused) {
        final double v = MapVariables.WORLD_WIDTH / mapVariables.getScale();

        final double degreesPerPixel = v / mapVariables.getViewWidth();

        context.setLineDashes(null);
        context.setStroke(color);
        context.setFill(color);

        final double x1 = 0.5 + mapVariables.getViewWidth() - EDGE_OFFSET - MAX_WIDTH;
        final double x2 = x1 + MAX_WIDTH;

        final double y = 0.5 + mapVariables.getViewHeight() - EDGE_OFFSET;

        painterHelper.strokeLine(context, x1, y, x2, y);
        painterHelper.strokeLine(context, x1, y - 1, x1, y - LIMIT_LENGTH);
        painterHelper.strokeLine(context, x2, y - 1, x2, y - LIMIT_LENGTH);

        final String s;

        final double degrees = degreesPerPixel * MAX_WIDTH;
        final double arcminutes = degrees * 60;
        final double arcseconds = arcminutes * 60;
        if (degrees >= 1) {
            s = "%s°%s'".formatted((int) degrees, (int) (arcminutes % 60));
        } else {
            if (arcminutes >= 1) {
                s = "%s'%s\"".formatted((int) arcminutes, (int) (arcseconds % 60));
            } else {
                s = "%s.%s\"".formatted((int) arcseconds, (int) ((arcseconds * 60) % 60));
            }
        }

        context.setTextAlign(TextAlignment.CENTER);
        context.setTextBaseline(VPos.BOTTOM);
        painterHelper.fillText(context, s, (x1 + x2) / 2.0, y - 5);
    }
}
