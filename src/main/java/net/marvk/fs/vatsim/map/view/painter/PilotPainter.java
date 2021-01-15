package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class PilotPainter extends MapPainter<Pilot> {
    private static final int MULTI_DRAW_BOUND = 10;
    private static final int TEXT_OFFSET = 11;
    private static final int RECT_SIZE = 4;
    private static final int SPEED_THRESHOLD = 25;
    private static final double MAX_SPEED = 600.0;
    private static final double SCALE_SCALE = 64.0;

    @Parameter("Label")
    private boolean label = true;
    @Parameter("Label Color")
    private Color labelColor = Color.valueOf("3b3526").deriveColor(0, 1, 3, 0.25);

    @Parameter("Background")
    private boolean paintBackground = false;
    @Parameter("Background Color")
    private Color backgroundColor;

    @Parameter(value = "Head Max Length", min = 0)
    private int headLength = 8;

    @Parameter(value = "Tail Max Length", min = 0)
    private int tailLength = 50;

    @Parameter(value = "Head/Tail length scale fixed")
    private boolean headTailFixed = false;

    @Parameter(value = "Head/Tail length scaled with speed")
    private boolean headTailScaledWithSpeed = true;

    public PilotPainter(final MapVariables mapVariables, final Color labelColor, final Color backgroundColor) {
        super(mapVariables);
        this.labelColor = labelColor;
        this.backgroundColor = backgroundColor;
        this.paintBackground = true;
    }

    public PilotPainter(final MapVariables mapVariables, final Color labelColor, final boolean paintBackground) {
        super(mapVariables);
        this.labelColor = labelColor;
        this.paintBackground = paintBackground;
        setBackgroundColor();
    }

    public PilotPainter(final MapVariables mapVariables) {
        super(mapVariables);
        setBackgroundColor();
    }

    private void setBackgroundColor() {
        backgroundColor = labelColor.deriveColor(0, 1, 0.5, 1);
    }

    @Override
    public void paint(final GraphicsContext c, final Pilot client) {
        final Point2D position = client.getPosition();

        final double centerX = mapVariables.toCanvasX(position.getX());

        if (centerX - MULTI_DRAW_BOUND < 0) {
            draw(c, client, 360);
        }

        if (centerX + MULTI_DRAW_BOUND > mapVariables.getViewWidth()) {
            draw(c, client, -360);
        }

        draw(c, client, 0);
    }

    private void draw(final GraphicsContext c, final Pilot pilot, final int xOffset) {
        final Point2D point = pilot.getPosition();

        final double x = mapVariables.toCanvasX(point.getX() + xOffset);
        final double y = mapVariables.toCanvasY(point.getY());

        c.setLineDashes();
        c.setStroke(labelColor);
        c.setFill(labelColor);
        c.setLineWidth(1);
        painterHelper.strokeRect(c, (int) x - 1.5, (int) y - 1.5, RECT_SIZE, RECT_SIZE);
        final double heading = pilot.getHeading();

        final double speedScale = speedScale(pilot);
        final double actualHeadLength = getActualHeadLength(this.headLength, speedScale);
        final double actualTailLength = getActualHeadLength(this.tailLength, speedScale);

        if (actualHeadLength > 0) {
            paintLine(c, x, y, heading, actualHeadLength);
        }

        if (actualTailLength > 0) {
            final double scale = mapVariables.getScale() / 64.;
            c.setLineDashes((double) 1 / 16 * scale, 1 * scale);
            c.setLineWidth(Math.min(1, (1.0 / 8) * scale));
            paintLine(c, x, y, 180 + heading, actualTailLength);
        }

        if (label) {
            final int yOffset;

            if (heading >= 90 && heading <= 270) {
                yOffset = -TEXT_OFFSET;
            } else {
                yOffset = TEXT_OFFSET;
            }

            c.setTextAlign(TextAlignment.CENTER);
            painterHelper.fillTextWithBackground(
                    c,
                    (int) x,
                    (int) y + yOffset,
                    pilot.getCallsign(),
                    paintBackground,
                    VPos.CENTER,
                    labelColor,
                    backgroundColor
            );
        }
    }

    private double speedScale(final Pilot pilot) {
        if (headTailScaledWithSpeed) {
            if (pilot.getGroundSpeed() < SPEED_THRESHOLD) {
                return 0;
            }
            return pilot.getGroundSpeed() / MAX_SPEED;
        }
        return 1;
    }

    private double getActualHeadLength(final int headLength, final double speedScale) {
        if (headTailFixed) {
            return speedScale * headLength;
        }
        return speedScale * mapVariables.getScale() * headLength / SCALE_SCALE;
    }

    private void paintLine(final GraphicsContext c, final double x, final double y, final double heading, final double length) {
        final double rad = Math.toRadians(heading);
        final double x2 = ((int) x + Math.sin(rad) * length);
        final double y2 = ((int) y - Math.cos(rad) * length);

        painterHelper.strokeLine(c, x2 + 0.5, y2 + 0.5, (int) x + 0.5, (int) y + 0.5);
    }
}
