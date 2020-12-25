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

public class FirPainter extends MapPainter<FlightInformationRegionBoundary> {
    @Parameter("Fill Color")
    private final Color fillColor;
    @Parameter("Color")
    private final Color color;
    @Parameter(value = "Line Width", min = 0, max = 10)
    private final double lineWidth;
    @Parameter("Fill")
    private final boolean fill;
    @Parameter("Label")
    private final boolean label;

    private final Set<FlightInformationRegionBoundary> paintedFirbs = new HashSet<>();

    public FirPainter(final MapVariables mapVariables, final Color color, final double lineWidth, final boolean fill, final boolean label) {
        super(mapVariables);
        this.color = color;
        this.lineWidth = lineWidth;
        this.fill = fill;
        this.label = label;
        this.fillColor = color.deriveColor(0, 1, 1, 0.05);
    }

    public FirPainter(final MapVariables mapVariables, final Color color, final double lineWidth) {
        this(mapVariables, color, lineWidth, false, false);
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

        c.setStroke(color);
        c.setFill(color);

        final Point2D polyLabel = polygon.getExteriorRing().getPolyLabel();
        if (label && polyLabel != null) {
            c.setTextAlign(TextAlignment.CENTER);
            c.setTextBaseline(VPos.CENTER);
            c.fillText(
                    firb.icaoProperty().get(),
                    mapVariables.toCanvasX(polyLabel.getX()),
                    mapVariables.toCanvasY(polyLabel.getY())
            );
        }

        c.setLineWidth(lineWidth);
        c.setLineDashes(null);
        painterHelper.strokePolygons(c, polygon);
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
