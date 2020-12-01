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

    public AirportPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final GraphicsContext c, final Airport airport) {
        if (airport.hasControllers()) {
            final Point2D point = airport.getPosition();
            final double x = (int) mapVariables.toCanvasX(point.getX());
            final double y = (int) mapVariables.toCanvasY(point.getY());
            c.setFill(Color.GREY);
            c.setTextAlign(TextAlignment.CENTER);
            c.setTextBaseline(VPos.CENTER);
            c.fillText(airport.getIcao(), x, y - 6);
            c.fillRect(x, y, 1, 1);

            new ArrayList<>(airport.getControllers());

            final List<ControllerType> types = airport
                    .getControllers()
                    .stream()
                    .map(Controller::getControllerType)
                    .distinct()
                    .sorted(Comparator.comparingInt(Enum::ordinal))
                    .collect(Collectors.toCollection(ArrayList::new));

            if (types.remove(ControllerType.DEP) | types.remove(ControllerType.APP)) {
                //paint appr dept
            }

            for (int i = 0; i < types.size(); i++) {
                final ControllerType type = types.get(i);
                c.setFill(color(type));
                c.setStroke(Color.BLACK.brighter());
                final double xCur = 1 + x + i * (TYPES_WIDTH + 1) - (types.size() / 2.0) * (TYPES_WIDTH + 1);
                final double yCur = y + 3;
                c.fillRect(xCur, yCur, TYPES_WIDTH, TYPES_WIDTH);

                c.setFill(Color.WHITE);
                c.fillText(type.toString().substring(0, 1), xCur + TYPES_WIDTH / 2.0, yCur + TYPES_WIDTH / 2.0);

                c.strokeRect(xCur - 0.5, yCur - 0.5, TYPES_WIDTH + 1, TYPES_WIDTH + 1);
            }
        }
    }

    private static Color color(final ControllerType type) {
        return switch (type) {
            case ATIS -> Color.web("443000");
            case DEL -> Color.web("004600");
            case GND -> Color.web("004D72");
            case TWR -> Color.web("760023");
            default -> Color.GREY;
        };
    }
}
