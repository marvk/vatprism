package net.marvk.fs.vatsim.map.view.painter;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AirportPainter extends MapPainter<Airport> {
    private static final int TYPES_WIDTH = 9;

    @Parameter("Paint Uncontrolled Airports")
    private boolean paintAll = false;

    @Parameter("Paint Uncontrolled Airports with Destinations or Arrivals")
    private boolean paintUncontrolledButDestinationsOrArrivals = false;

    @Parameter("Airport Color")
    private Color airportColor = Color.WHITE;

    @Parameter("Label")
    private boolean text = true;
    @Parameter("Label Color")
    private Color textColor = Color.web("80334d");

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
    private Color appColor = Color.web("005757");
    @Parameter("Approach Placeholder Color")
    private Color appPlaceholderColor = Color.web("17130a");
    @Parameter(value = "Approach Radius", min = 0.25, max = 10)
    private double approachRadius = 1;
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

        c.setLineDashes(null);
        c.setLineWidth(1);

        final String icao = airport.getIcao();
        final int departuresOnGround = airport.getDeparturesOnGround();
        final int arrivalsOnGround = airport.getArrivalsOnGround();

        final double textScale = c.getFont().getSize() / 12.0;
        final boolean paintApproachCircle = mapVariables.getScale() > (40 / approachRadius) * textScale;
        final boolean paintApproachLabel = paintApproachCircle;
        final boolean paintApproach = types.remove(ControllerType.DEP) | types.remove(ControllerType.APP);

        if (paintControllers) {
            c.setTextAlign(TextAlignment.CENTER);
            if (paintApproach) {
                final double r = approachRadius * mapVariables.getScale();
                final double rHalf = r / 2.0;

                if (paintApproachCircle) {
                    c.setStroke(appColor);
                    painterHelper.strokeOval(c, x - rHalf, y - rHalf, r, r);
                }

                if (paintApproachLabel && text) {
                    c.setFill(appColor);
                    c.setTextBaseline(VPos.BOTTOM);
                    painterHelper.fillText(c, icao, x, y - rHalf);
                }
            }
        }

        c.setFill(airportColor);
        painterHelper.fillRect(c, x - 2, y, 5, 1);
        painterHelper.fillRect(c, x, y - 2, 1, 5);

        if (paintControllers) {
            c.setTextBaseline(VPos.TOP);
            if (paintApproach && types.isEmpty() && !paintApproachCircle) {
                types.add(ControllerType.APP);
            }

            final int n = types.size();

            final int typesWidth = (int) Math.ceil(textScale * TYPES_WIDTH);
            for (int i = 0; i < n; i++) {
                final ControllerType type = types.get(i);
                c.setFill(color(type));
                c.setStroke(typesBorderColor);
                final double xCur = typeLabelX(x, n, i, typesWidth);
                final double yCur = typeLabelY(y);
                painterHelper.fillRect(c, xCur, yCur, typesWidth, typesWidth);

                if (type != ControllerType.APP) {
                    c.setFill(typesLabelColor);
                    painterHelper.fillText(
                            c,
                            type.toString().substring(0, 1),
                            xCur + typesWidth / 2.0,
                            yCur - 3
                    );
                }

                painterHelper.strokeRect(c, xCur - 0.5, yCur - 0.5, typesWidth + 1, typesWidth + 1);
            }

            if (paintApproach && !paintApproachCircle) {
                c.setStroke(appColor);
                final double xCur = typeLabelX(x, n, 0, typesWidth);
                final double yCur = typeLabelY(y);

                painterHelper.strokeRect(c, xCur - 1.5, yCur - 1.5, n * (typesWidth + 1) + 2, typesWidth + 3);
            }
        }

        final int typesWidth = (int) Math.ceil(textScale * TYPES_WIDTH);
        if (departuresOnGround > 0) {
            c.setFill(Color.GREEN);
            c.setTextBaseline(VPos.TOP);
            final double xCur = typeLabelX(x, 3, 0, typesWidth);
            final double yCur = y - 30;
            painterHelper.fillText(c, "⬈" + departuresOnGround, xCur, yCur);
        }

        if (arrivalsOnGround > 0) {
            c.setFill(Color.RED);
            c.setTextBaseline(VPos.TOP);
            final double xCur = typeLabelX(x, 3, 3, typesWidth);
            final double yCur = y - 30;
            painterHelper.fillText(c, "⬊" + arrivalsOnGround, xCur, yCur);
        }

        if (text) {
            painterHelper.fillTextWithBackground(
                    c,
                    x,
                    labelY(y),
                    icao,
                    paintBackground,
                    TextAlignment.CENTER,
                    VPos.BOTTOM,
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

    private double labelY(final double y) {
        return y - 4;
    }

    private double typeLabelY(final double y) {
        return y + 6;
    }

    private double typeLabelX(final double x, final int n, final int i, final double typesWidth) {
        return 1 + x + i * (typesWidth + 1) - (n / 2.0) * (typesWidth + 1);
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
