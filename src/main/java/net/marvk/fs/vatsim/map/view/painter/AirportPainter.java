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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AirportPainter extends MapPainter<Airport> {
    private static final int TYPES_WIDTH = 9;
    private static final int APPROACH_RADIUS = 1;
    private static final Color APPROACH_COLOR = Color.CYAN.darker().darker().darker();
    private static final Color TYPES_LABEL_COLOR = Color.WHITE.darker();
    private static final Color TYPES_BORDER_COLOR = Color.BLACK.brighter();

    private static final Color ATIS_COLOR = Color.web("443000");
    private static final Color DEL_COLOR = Color.web("004600");
    private static final Color GND_COLOR = Color.web("004D72");
    private static final Color TWR_COLOR = Color.web("760023");
    private static final Color APP_COLOR = Color.web("17130a");

    public AirportPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final GraphicsContext c, final Airport airport) {
        if (airport.hasControllers()) {
            final Point2D point = airport.getPosition();
            final double x = (int) mapVariables.toCanvasX(point.getX());
            final double y = (int) mapVariables.toCanvasY(point.getY());

            final List<ControllerType> types = airport
                    .getControllers()
                    .stream()
                    .map(Controller::getControllerType)
                    .distinct()
                    .sorted(Comparator.comparingInt(Enum::ordinal))
                    .collect(Collectors.toCollection(ArrayList::new));

            c.setTextAlign(TextAlignment.CENTER);
            c.setTextBaseline(VPos.BOTTOM);

            final String icao = airport.getIcao();

            final boolean paintApproachCircle = mapVariables.getScale() > 32;
            final boolean paintApproachLabel = mapVariables.getScale() > 32;

            final boolean paintApproach = types.remove(ControllerType.DEP) | types.remove(ControllerType.APP);
            if (paintApproach) {
                final double r = APPROACH_RADIUS * mapVariables.getScale();
                final double rHalf = r / 2.0;

                if (paintApproachCircle) {
                    c.setStroke(APPROACH_COLOR);
                    c.strokeOval(x - rHalf, y - rHalf, r, r);
                }

                if (paintApproachLabel) {
                    c.setFill(APPROACH_COLOR);
                    c.fillText(icao, x, y - rHalf);
                }
            }

            c.setTextBaseline(VPos.CENTER);
            c.setFill(Color.GREY);
            c.fillText(icao, x, y - 6);
            c.fillRect(x, y, 1, 1);

            if (paintApproach && types.isEmpty() && !paintApproachCircle) {
                types.add(ControllerType.APP);
            }

            final int n = types.size();

            for (int i = 0; i < n; i++) {
                final ControllerType type = types.get(i);
                c.setFill(color(type));
                c.setStroke(TYPES_BORDER_COLOR);
                final double xCur = labelsX(x, n, i);
                final double yCur = labelsY(y);
                c.fillRect(xCur, yCur, TYPES_WIDTH, TYPES_WIDTH);

                if (type != ControllerType.APP) {
                    c.setFill(TYPES_LABEL_COLOR);
                    c.fillText(type.toString().substring(0, 1), xCur + TYPES_WIDTH / 2.0, yCur + TYPES_WIDTH / 2.0);
                }

                c.strokeRect(xCur - 0.5, yCur - 0.5, TYPES_WIDTH + 1, TYPES_WIDTH + 1);
            }

            if (paintApproach && !paintApproachCircle) {
                c.setStroke(APPROACH_COLOR);
                final double xCur = labelsX(x, n, 0);
                final double yCur = labelsY(y);

                c.strokeRect(xCur - 1.5, yCur - 1.5, n * (TYPES_WIDTH + 1) + 2, TYPES_WIDTH + 3);
            }
        }
    }

    private double labelsY(final double y) {
        return y + 4;
    }

    private double labelsX(final double x, final int n, final int i) {
        return 1 + x + i * (TYPES_WIDTH + 1) - (n / 2.0) * (TYPES_WIDTH + 1);
    }

    private static Color color(final ControllerType type) {
        return switch (type) {
            case ATIS -> ATIS_COLOR;
            case DEL -> DEL_COLOR;
            case GND -> GND_COLOR;
            case TWR -> TWR_COLOR;
            case APP -> APP_COLOR;
            default -> Color.GREY;
        };
    }
}
