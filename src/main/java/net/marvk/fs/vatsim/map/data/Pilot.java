package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.api.data.VatsimPilot;
import net.marvk.fs.vatsim.map.GeomUtil;

public class Pilot extends Client implements Data {
    private final FlightPlan flightPlan = new FlightPlan(this);

    private final StringProperty transponder = new SimpleStringProperty();
    private final DoubleProperty altitude = new SimpleDoubleProperty();
    private final DoubleProperty groundSpeed = new SimpleDoubleProperty();
    private final DoubleProperty heading = new SimpleDoubleProperty();
    private final DoubleProperty qnhInchesMercury = new SimpleDoubleProperty();
    private final DoubleProperty qnhMilliBars = new SimpleDoubleProperty();
    private final ObjectProperty<Point2D> position = new SimpleObjectProperty<>();

    @Override
    public void setFromModel(final VatsimClient client) {
        final VatsimPilot pilot = (VatsimPilot) client;
        super.setFromModel(client);

        flightPlan.setFromModel(((VatsimPilot) client).getFlightPlan());

        transponder.set(pilot.getTransponder());
        altitude.set(Double.parseDouble(pilot.getAltitude()));
        groundSpeed.set(Double.parseDouble(pilot.getGroundSpeed()));
        heading.set(Double.parseDouble(pilot.getHeading()));
        qnhInchesMercury.set(Double.parseDouble(pilot.getQnhInchesMercury()));
        qnhMilliBars.set(Double.parseDouble(pilot.getQnhMillibars()));
        position.set(GeomUtil.parsePoint(pilot.getLongitude(), pilot.getLatitude()));
    }

    @Override
    public ClientType clientType() {
        return ClientType.PILOT;
    }

    public FlightPlan getFlightPlan() {
        return flightPlan;
    }

    public String getTransponder() {
        return transponder.get();
    }

    public ReadOnlyStringProperty transponderProperty() {
        return transponder;
    }

    public double getAltitude() {
        return altitude.get();
    }

    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    public double getGroundSpeed() {
        return groundSpeed.get();
    }

    public ReadOnlyDoubleProperty groundSpeedProperty() {
        return groundSpeed;
    }

    public double getHeading() {
        return heading.get();
    }

    public ReadOnlyDoubleProperty headingProperty() {
        return heading;
    }

    public double getQnhInchesMercury() {
        return qnhInchesMercury.get();
    }

    public ReadOnlyDoubleProperty qnhInchesMercuryProperty() {
        return qnhInchesMercury;
    }

    public double getQnhMilliBars() {
        return qnhMilliBars.get();
    }

    public ReadOnlyDoubleProperty qnhMilliBarsProperty() {
        return qnhMilliBars;
    }

    public Point2D getPosition() {
        return position.get();
    }

    public ReadOnlyObjectProperty<Point2D> positionProperty() {
        return position;
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
