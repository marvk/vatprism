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

    @Parameter(name = "Show Uncontrolled Airports", legacyName = "Paint Uncontrolled Airports")
    private boolean paintAll = false;

    @Parameter(name = "Show Labels of Uncontrolled Airports", legacyName = "Paint Labels of Uncontrolled Airports")
    private boolean paintAllLabels = true;

    @Parameter(name = "Show Uncontrolled Airports with Arrivals or Departures", legacyName = "Paint Uncontrolled Airports with Arrivals or Departures")
    private boolean paintUncontrolledButDestinationsOrArrivals = false;

    @Parameter(name = "Show Labels of Uncontrolled Airports with Arrivals or Departures", legacyName = "Paint Labels of Uncontrolled Airports with Arrivals or Departures")
    private boolean paintUncontrolledButDestinationsOrArrivalLabels = true;

    @Parameter(name = "Airport Marker Color", hintText = "The color of the airport marker")
    private Color airportColor = Color.WHITE;

    @Parameter(name = "Show Label", group = "Label")
    private boolean text = true;
    @Parameter(name = "Label Color", group = "Label")
    private Color textColor = Color.web("80334d");
    @Parameter(name = "Show Background", group = "Label", hintText = "Show a background behind the airport label")
    private boolean paintBackground = true;
    @Parameter(name = "Background Color", group = "Label")
    private Color backgroundColor;

    @Parameter(name = "Show Controllers", group = "Controllers", hintText = "Show active controllers above the airport marker")
    private boolean paintControllers = true;
    @Parameter(name = "Atis Color", group = "Controllers")
    private Color atisColor = Color.web("443000");
    @Parameter(name = "Delivery Color", group = "Controllers")
    private Color delColor = Color.web("004600");
    @Parameter(name = "Ground Color", group = "Controllers")
    private Color gndColor = Color.web("004D72");
    @Parameter(name = "Tower Color", group = "Controllers")
    private Color twrColor = Color.web("760023");
    @Parameter(name = "Approach Placeholder Color", group = "Controllers", hintText = "Color of the box that, when sufficiently zoomed out, is displayed above an airport when doesn't have other controllers")
    private Color appPlaceholderColor = Color.web("17130a");
    @Parameter(name = "Controller Label Color", group = "Controllers", hintText = "Color of the controller glyphs above the airport marker")
    private Color typesLabelColor = Color.WHITE.darker();
    @Parameter(name = "Controller Border Color", group = "Controllers")
    private Color typesBorderColor = Color.BLACK.brighter();

    @Parameter(name = "Approach Color", group = "Approach")
    private Color appColor = Color.web("005757");
    @Parameter(name = "Approach Radius", group = "Approach", min = 0.25, max = 10, hintText = "Radius of the circle that, when sufficiently zoomed in, shows an airport has an active approach controller")
    private double approachRadius = 1;


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

        if (text) {
            final boolean uncontrolledButDestinationOriginEnabled = (airport.hasDepartures() || airport.hasArrivals()) && paintUncontrolledButDestinationsOrArrivalLabels;
            if (paintAllLabels || airport.hasControllers() || uncontrolledButDestinationOriginEnabled) {
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
