package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.data.ControllerType;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AirportPainter extends MapPainter<Airport> {
    private static final int TYPES_WIDTH = 9;
    private static final int APPROACH_RADIUS = 1;

    @Parameter("Paint Uncontrolled Airports")
    private boolean paintAll = false;

    @Parameter("Paint Uncontrolled Airports with Destinations or Arrivals")
    private boolean paintUncontrolledButDestinationsOrArrivals = false;

//    @Parameter("Airport Color")
    private Color airportColor = Color.GREY;

    @Parameter("Label")
    private boolean text = true;
    @Parameter("Label Color")
    private Color textColor = Color.GREY;

    @Parameter("Background")
    private boolean paintBackground = true;
    @Parameter("Background Color")
    private Color backgroundColor;

    @Parameter("Controllers")
    private boolean paintControllers = true;
    @Parameter("Atis Color")
    private Color atisColor = Color.web("443000");
    @Parameter("Delivery Color")
    private Color delColor = Color.web("004600");
    @Parameter("Ground Color")
    private Color gndColor = Color.web("004D72");
    @Parameter("Tower Color")
    private Color twrColor = Color.web("760023");
    @Parameter("Approach Color")
    private Color appColor = Color.CYAN.darker().darker().darker();
    @Parameter("Approach PlaceholderColor")
    private Color appPlaceholderColor = Color.web("17130a");
    @Parameter("Controller Label Color")
    private Color typesLabelColor = Color.WHITE.darker();
    @Parameter("Controller Border Color")
    private Color typesBorderColor = Color.BLACK.brighter();

    public AirportPainter(final MapVariables mapVariables) {
        super(mapVariables);
        setBackgroundColor();
    }

    public AirportPainter(final MapVariables mapVariables, final Color textColor, final Color airportColor, final boolean paintAll, final boolean paintControllers, final boolean paintBackground) {
        super(mapVariables);
        this.textColor = textColor;
        this.airportColor = airportColor;
        this.paintAll = paintAll;
        this.paintControllers = paintControllers;
        this.paintBackground = paintBackground;
        setBackgroundColor();
    }

    private void setBackgroundColor() {
        backgroundColor = textColor.deriveColor(0, 1, 0.5, 1);
    }

    @Override
    public void paint(final GraphicsContext c, final Airport airport) {
        final Point2D position = airport.getPosition();
        final double xOffset = xOffset(position);

        if (!mapVariables.isContainedInExpandedWorldView(position.getX() + xOffset, position.getY())) {
            return;
        }

        if (!paintAll &&
                !airport.hasControllers() &&
                (!paintUncontrolledButDestinationsOrArrivals || (!airport.hasArrivals() && !airport.hasDepartures()))
        ) {
            return;
        }

        draw(c, airport, xOffset);
    }

    private void draw(final GraphicsContext c, final Airport airport, final double xOffset) {
        final Point2D point = airport.getPosition();
        final double x = (int) mapVariables.toCanvasX(point.getX() + xOffset);
        final double y = (int) mapVariables.toCanvasY(point.getY());

        final List<ControllerType> types = airport
                .getControllers()
                .stream()
                .map(Controller::getControllerType)
                .distinct()
                .sorted(ControllerType.COMPARATOR)
                .collect(Collectors.toCollection(ArrayList::new));

        c.setTextAlign(TextAlignment.CENTER);
        c.setTextBaseline(VPos.BOTTOM);
        c.setLineDashes(null);
        c.setLineWidth(1);

        final String icao = airport.getIcao();

        final boolean paintApproachCircle = mapVariables.getScale() > 32;
        final boolean paintApproachLabel = mapVariables.getScale() > 32;
        final boolean paintApproach = types.remove(ControllerType.DEP) | types.remove(ControllerType.APP);

        if (paintControllers) {
            if (paintApproach) {
                final double r = APPROACH_RADIUS * mapVariables.getScale();
                final double rHalf = r / 2.0;

                if (paintApproachCircle) {
                    c.setStroke(appColor);
                    painterHelper.strokeOval(c, x - rHalf, y - rHalf, r, r);
                }

                if (paintApproachLabel && text) {
                    c.setFill(appColor);
                    painterHelper.fillText(c, icao, x, y - rHalf);
                }
            }
        }

        c.setFill(textColor);
        painterHelper.fillRect(c, x, y, 1, 1);

        c.setTextBaseline(VPos.CENTER);
        if (paintControllers) {
            if (paintApproach && types.isEmpty() && !paintApproachCircle) {
                types.add(ControllerType.APP);
            }

            final int n = types.size();

            for (int i = 0; i < n; i++) {
                final ControllerType type = types.get(i);
                c.setFill(color(type));
                c.setStroke(typesBorderColor);
                final double xCur = labelsX(x, n, i);
                final double yCur = labelsY(y);
                painterHelper.fillRect(c, xCur, yCur, TYPES_WIDTH, TYPES_WIDTH);

                if (type != ControllerType.APP) {
                    c.setFill(typesLabelColor);
                    painterHelper.fillText(
                            c,
                            type.toString().substring(0, 1),
                            xCur + TYPES_WIDTH / 2.0,
                            yCur + TYPES_WIDTH / 2.0
                    );
                }

                painterHelper.strokeRect(c, xCur - 0.5, yCur - 0.5, TYPES_WIDTH + 1, TYPES_WIDTH + 1);
            }

            if (paintApproach && !paintApproachCircle) {
                c.setStroke(appColor);
                final double xCur = labelsX(x, n, 0);
                final double yCur = labelsY(y);

                painterHelper.strokeRect(c, xCur - 1.5, yCur - 1.5, n * (TYPES_WIDTH + 1) + 2, TYPES_WIDTH + 3);
            }
        }

        if (text) {
            painterHelper.fillTextWithBackground(
                    c,
                    x,
                    y - 10,
                    icao,
                    paintBackground,
                    TextAlignment.CENTER,
                    VPos.CENTER,
                    textColor,
                    backgroundColor
            );
        }
    }

    private double xOffset(final Point2D point) {
        final double centerX = mapVariables.toCanvasX(point.getX());

        if (centerX < 0) {
            return 360;
        } else if (centerX > mapVariables.getViewWidth()) {
            return -360;
        } else {
            return 0;
        }
    }

    private double labelsY(final double y) {
        return y + 4;
    }

    private double labelsX(final double x, final int n, final int i) {
        return 1 + x + i * (TYPES_WIDTH + 1) - (n / 2.0) * (TYPES_WIDTH + 1);
    }

    private Color color(final ControllerType type) {
        return switch (type) {
            case ATIS -> atisColor;
            case DEL -> delColor;
            case GND -> gndColor;
            case TWR -> twrColor;
            case APP -> appPlaceholderColor;
            default -> Color.GREY;
        };
    }
}
