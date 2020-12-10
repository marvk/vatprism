package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import lombok.ToString;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.api.data.VatsimController;

import java.util.List;

@ToString
public class Controller extends Client implements Data {
    private final StringProperty frequency = new SimpleStringProperty();
    private final StringProperty rating = new SimpleStringProperty();
    private final StringProperty atisMessage = new SimpleStringProperty();

    private final ObjectProperty<ControllerType> controllerType = new SimpleObjectProperty<>();

    private final ReadOnlyObjectWrapper<Airport> workingAirport =
            RelationshipReadOnlyObjectWrapper.withOtherList(this, Airport::getControllersWritable);

    private final ReadOnlyObjectWrapper<FlightInformationRegion> workingFlightInformationRegion =
            RelationshipReadOnlyObjectWrapper.withOtherList(this, FlightInformationRegion::getControllersWritable);

    private final ReadOnlyObjectWrapper<UpperInformationRegion> workingUpperInformationRegion =
            RelationshipReadOnlyObjectWrapper.withOtherList(this, UpperInformationRegion::getControllersWritable);

    @Override
    public void setFromModel(final VatsimClient client) {
        final VatsimController controller = (VatsimController) client;
        super.setFromModel(controller);

        frequency.set(controller.getFrequency());
        rating.set("%s (%s)".formatted(controller.getRating().getLongName(), controller.getRating().getShortName()));
        setAtisMessage(controller.getTextAtis());
    }

    private void setAtisMessage(final List<String> textAtis) {
        if (textAtis != null) {
            final String msg = String
                    .join(" ", textAtis);
            this.atisMessage.set(msg);
        }
    }

    public void setFromCallsignParserResult(final CallsignParser.Result result) {
        controllerType.set(result.getControllerType());
        workingAirport.set(result.getAirport());
        workingFlightInformationRegion.set(result.getFlightInformationRegion());
        workingUpperInformationRegion.set(result.getUpperInformationRegion());
    }

    public Data getWorkingArea() {
        if (getWorkingAirport() != null) {
            return getWorkingAirport();
        }

        if (getWorkingFlightInformationRegion() != null) {
            return getWorkingFlightInformationRegion().getBoundary();
        }

        if (getWorkingUpperInformationRegion() != null) {
            return getWorkingUpperInformationRegion();
        }

        return null;
    }

    public String getFrequency() {
        return frequency.get();
    }

    public ReadOnlyStringProperty frequencyProperty() {
        return frequency;
    }

    public String getRating() {
        return rating.get();
    }

    public ReadOnlyStringProperty ratingProperty() {
        return rating;
    }

    public String getAtisMessage() {
        return atisMessage.get();
    }

    public ReadOnlyStringProperty atisMessageProperty() {
        return atisMessage;
    }

    public ControllerType getControllerType() {
        return controllerType.get();
    }

    public ReadOnlyObjectProperty<ControllerType> controllerTypeProperty() {
        return controllerType;
    }

    public Airport getWorkingAirport() {
        return workingAirport.get();
    }

    ObjectProperty<Airport> workingAirportPropertyWritable() {
        return workingAirport;
    }

    public ReadOnlyObjectProperty<Airport> workingAirportProperty() {
        return workingAirport.getReadOnlyProperty();
    }

    public FlightInformationRegion getWorkingFlightInformationRegion() {
        return workingFlightInformationRegion.get();
    }

    ObjectProperty<FlightInformationRegion> workingFlightInformationRegionPropertyWritable() {
        return workingFlightInformationRegion;
    }

    public ReadOnlyObjectProperty<FlightInformationRegion> workingFlightInformationRegionProperty() {
        return workingFlightInformationRegion.getReadOnlyProperty();
    }

    public UpperInformationRegion getWorkingUpperInformationRegion() {
        return workingUpperInformationRegion.get();
    }

    ObjectProperty<UpperInformationRegion> workingUpperInformationRegionPropertyWritable() {
        return workingUpperInformationRegion;
    }

    public ReadOnlyObjectProperty<UpperInformationRegion> workingUpperInformationRegionProperty() {
        return workingUpperInformationRegion.getReadOnlyProperty();
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
