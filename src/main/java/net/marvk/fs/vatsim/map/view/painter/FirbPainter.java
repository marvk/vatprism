package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import javafx.scene.text.TextAlignment;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.Polygon;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.HashSet;
import java.util.Set;

public class FirbPainter extends MapPainter<FlightInformationRegionBoundary> {
    private static final int MULTI_DRAW_BOUND = 20;
    @Parameter(name = "Fill", group = "Fill")
    private boolean fill;
    @Parameter(name = "Fill Color", group = "Fill")
    private Color fillColor;
    @Parameter(name = "Stroke", group = "Stroke")
    private boolean stroke;
    @Parameter(name = "Stroke Color", group = "Stroke")
    private Color strokeColor;
    @Parameter(name = "Stroke Width", group = "Stroke", min = 0, max = 10)
    private double lineWidth;
    @Parameter(name = "Label", group = "Label")
    private boolean label;
    @Parameter(name = "Label Color", group = "Label")
    private Color textColor;

    private final Set<FlightInformationRegionBoundary> paintedFirbs = new HashSet<>();

    public FirbPainter(final MapVariables mapVariables, final Color strokeColor, final double lineWidth, final boolean fill, final boolean stroke, final boolean label) {
        super(mapVariables);
        this.strokeColor = strokeColor;
        this.lineWidth = lineWidth;
        this.fill = fill;
        this.label = label;
        this.stroke = stroke;
        this.fillColor = strokeColor.deriveColor(0, 1, 1, 0.05);
        this.textColor = strokeColor;
    }

    public FirbPainter(final MapVariables mapVariables, final Color strokeColor, final double lineWidth) {
        this(mapVariables, strokeColor, lineWidth, false, true, false);
    }

    @Override
    public void afterAllRender() {
        paintedFirbs.clear();
    }

    @Override
    public void paint(final GraphicsContext c, final FlightInformationRegionBoundary firb) {
        if (firb.isExtension()) {
            return;
        }

        if (!paintedFirbs.add(firb)) {
            return;
        }

        final Polygon polygon = firb.getPolygon();

        if (fill) {
            c.setFill(fillColor);
            painterHelper.fillPolygons(c, polygon);
        }

        if (stroke) {
            c.setStroke(strokeColor);
            c.setLineWidth(lineWidth);
            c.setLineDashes(null);
            painterHelper.strokePolygons(c, polygon);
        }

        if (label) {
            final double centerX = mapVariables.toCanvasX(firb.getPolygon().getExteriorRing().getPolyLabel().getX());
            if (centerX - MULTI_DRAW_BOUND < 0) {
                drawLabel(c, firb, 360);
            }

            if (centerX + MULTI_DRAW_BOUND > mapVariables.getViewWidth()) {
                drawLabel(c, firb, -360);
            }

            drawLabel(c, firb, 0);
        }
    }

    private void drawLabel(final GraphicsContext c, final FlightInformationRegionBoundary firb, final double offsetX) {
        final Point2D polyLabel = firb.getPolygon().getExteriorRing().getPolyLabel();

        if (polyLabel != null) {
            c.setFill(textColor);
            c.setTextAlign(TextAlignment.CENTER);
            c.setTextBaseline(VPos.CENTER);
            painterHelper.fillText(
                    c,
                    "%s%s".formatted(firb.getIcao(), firb.isOceanic() ? " Oceanic" : ""),
                    mapVariables.toCanvasX(polyLabel.getX() + offsetX),
                    mapVariables.toCanvasY(polyLabel.getY())
            );
        }
    }

    private Paint hatched(final FlightInformationRegionBoundary firb) {
        final double cx = mapVariables.toCanvasX(firb.getPolygon().boundary().getMinX());
        final double cy = mapVariables.toCanvasY(firb.getPolygon().boundary().getMinY());
        return new LinearGradient(
                cx,
                cy,
                cx + 10,
                cy + 10,
                false,
                CycleMethod.REPEAT,
                new Stop(0, fillColor),
                new Stop(0.5, fillColor),
                new Stop(0.5, fillColor.darker()),
                new Stop(1, fillColor.darker())
        );
    }
}
