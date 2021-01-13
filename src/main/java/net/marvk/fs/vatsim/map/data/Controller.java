package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.api.data.VatsimController;

import java.util.List;

@Log4j2
@ToString
public class Controller extends Client implements Data {
    private final StringProperty frequency = new SimpleStringProperty();
    private final StringProperty atisMessage = new SimpleStringProperty();

    private final ObjectProperty<ControllerRating> rating = new SimpleObjectProperty<>();

    private final ObjectProperty<ControllerType> controllerType = new SimpleObjectProperty<>();

    private final ObjectProperty<Data> workingLocation =
            new SimpleObjectProperty<>();

    private final ReadOnlyObjectWrapper<Airport> workingAirport =
            RelationshipReadOnlyObjectWrapper.withOtherList(this, Airport::getControllersWritable);

    private final ReadOnlyObjectWrapper<FlightInformationRegion> workingFlightInformationRegion =
            RelationshipReadOnlyObjectWrapper.withOtherList(this, FlightInformationRegion::getControllersWritable);

    private final ReadOnlyObjectWrapper<FlightInformationRegionBoundary> workingFlightInformationRegionBoundary =
            RelationshipReadOnlyObjectWrapper.withOtherList(this, FlightInformationRegionBoundary::getControllersWritable);

    private final ReadOnlyObjectWrapper<UpperInformationRegion> workingUpperInformationRegion =
            RelationshipReadOnlyObjectWrapper.withOtherList(this, UpperInformationRegion::getControllersWritable);

    @Override
    public void setFromModel(final VatsimClient client) {
        final VatsimController controller = (VatsimController) client;
        super.setFromModel(controller);

        frequency.set(controller.getFrequency());
        rating.set(ControllerRating.of(controller.getRating()));
        setAtisMessage(controller.getTextAtis());
    }

    @Override
    public ClientType clientType() {
        return ClientType.CONTROLLER;
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
        workingFlightInformationRegionBoundary.set(result.getFlightInformationRegionBoundary());
        workingUpperInformationRegion.set(result.getUpperInformationRegion());

        if (getWorkingAirport() != null) {
            workingLocation.set(getWorkingAirport());
        }

        if (getWorkingFlightInformationRegion() != null) {
            workingLocation.set(getWorkingFlightInformationRegionBoundary());
        }

        if (getWorkingUpperInformationRegion() != null) {
            workingLocation.set(getWorkingUpperInformationRegion());
        }
    }

    public Data getWorkingLocation() {
        return workingLocation.get();
    }

    public ReadOnlyObjectProperty<Data> workingLocationProperty() {
        return workingLocation;
    }

    public String getFrequency() {
        return frequency.get();
    }

    public ReadOnlyStringProperty frequencyProperty() {
        return frequency;
    }

    public ControllerRating getRating() {
        return rating.get();
    }

    public ReadOnlyObjectProperty<ControllerRating> ratingProperty() {
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

    public FlightInformationRegionBoundary getWorkingFlightInformationRegionBoundary() {
        return workingFlightInformationRegionBoundary.get();
    }

    ObjectProperty<FlightInformationRegionBoundary> workingFlightInformationRegionBoundaryPropertyWritable() {
        return workingFlightInformationRegionBoundary;
    }

    public ReadOnlyObjectProperty<FlightInformationRegionBoundary> workingFlightInformationRegionBoundaryProperty() {
        return workingFlightInformationRegionBoundary.getReadOnlyProperty();
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
