package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import net.marvk.fs.vatsim.api.data.VatsimClient;

public class Pilot extends Client {
    private final FlightPlan flightPlan = new FlightPlan(this);

    private final StringProperty transponder = new SimpleStringProperty();
    private final DoubleProperty altitude = new SimpleDoubleProperty();
    private final DoubleProperty groundSpeed = new SimpleDoubleProperty();
    private final DoubleProperty heading = new SimpleDoubleProperty();
    private final DoubleProperty qnhInchesMercury = new SimpleDoubleProperty();
    private final DoubleProperty qnhMilliBars = new SimpleDoubleProperty();

    @Override
    public void setFromModel(final VatsimClient model) {
        super.setFromModel(model);

        flightPlan.setFromModel(model);

        transponder.set(model.getTransponder());
        altitude.set(Double.parseDouble(model.getAltitude()));
        groundSpeed.set(Double.parseDouble(model.getGroundSpeed()));
        heading.set(Double.parseDouble(model.getHeading()));
        qnhInchesMercury.set(Double.parseDouble(model.getQnhInchesMercury()));
        qnhMilliBars.set(Double.parseDouble(model.getQnhMillibars()));
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
}
