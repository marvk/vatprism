//package net.marvk.fs.vatsim.map.view.painter;
//
//import javafx.beans.value.ObservableObjectValue;
//import javafx.geometry.Point2D;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.paint.Color;
//import net.marvk.fs.vatsim.map.data.Airport;
//import net.marvk.fs.vatsim.map.data.AirportViewModel;
//import net.marvk.fs.vatsim.map.data.ClientViewModel;
//import net.marvk.fs.vatsim.map.data.ControllerDataViewModel;
//import net.marvk.fs.vatsim.map.data.ControllerType;
//import net.marvk.fs.vatsim.map.view.map.MapVariables;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class AirportAtcPainter extends MapPainter<Map<Airport, List<ClientViewModel>>> {
//    private static final int APPROACH_RADIUS = 1;
//    private static final int RECT_WIDTH = 9;
//
//    public AirportAtcPainter(final MapVariables mapVariables) {
//        super(mapVariables);
//    }
//
//    @Override
//    public void paint(final GraphicsContext context, final Map<AirportViewModel, List<ClientViewModel>> map) {
//        map.forEach((airport, clients) -> paint(context, airport, clients));
//    }
//
//    private void paint(final GraphicsContext c, final AirportViewModel airport, final List<ClientViewModel> clients) {
//        if (airport.getModel() == null) {
//            return;
//        }
//
//        final List<ControllerType> types = clients
//                .stream()
//                .map(ClientViewModel::controllerData)
//                .map(ControllerDataViewModel::controllerTypeProperty)
//                .map(ObservableObjectValue::get)
//                .distinct()
//                .sorted(Comparator.comparingInt(Enum::ordinal))
//                .collect(Collectors.toCollection(ArrayList::new));
//
//        final Point2D position = mapVariables.toCanvas(airport.positionProperty().get());
//
//        c.setStroke(Color.DARKGRAY);
//        c.setLineDashes(null);
//        c.setLineWidth(1);
//        final double x_ = ((int) position.getX()) + 0.5;
//        final double y_ = ((int) position.getY()) + 0.5;
//        c.strokeLine(x_, y_, x_, y_);
//
//        c.setStroke(Color.CYAN.darker());
//        c.setFill(Color.CYAN.darker());
//
//        final double radius = (mapVariables.getScale() * APPROACH_RADIUS) + 0.5;
//        if (types.remove(ControllerType.APP) | types.remove(ControllerType.DEP)) {
//            c.strokeOval(x_ - radius, y_ - radius, radius * 2, radius * 2);
//        }
//
//        c.fillText(airport.icaoProperty().get(), x_, y_ - radius - 5);
//
//        types.forEach(System.out::println);
//        System.out.println();
//
//        final int n = types.size();
//
//        for (int i = 0; i < n; i++) {
//            final ControllerType controllerType = types.get(i);
//            final Color color = color(controllerType);
//
//            c.setFill(color);
//            c.fillRect(x_ - RECT_WIDTH - RECT_WIDTH / 2.0 + i * RECT_WIDTH, y_ + radius, RECT_WIDTH, RECT_WIDTH);
//        }
//    }
//
//    private Color color(final ControllerType type) {
//        return switch (type) {
//            case ATIS -> Color.YELLOW;
//            case DEL -> Color.BLUE;
//            case GND -> Color.GREEN;
//            case TWR -> Color.RED;
//            default -> Color.GREY;
//        };
//    }
//}
