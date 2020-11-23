package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import net.marvk.fs.vatsim.api.data.VatsimClient;

import java.time.ZonedDateTime;

public class ClientViewModel extends SimpleDataViewModel<VatsimClient, ClientViewModel> implements ViewModel {
    private final FlightPlanViewModel flightPlan;
    private final ClientStatusViewModel clientStatus;
    private final ControllerDataViewModel controllerData;

    @Inject
    public ClientViewModel(
            final FlightPlanViewModel flightPlan,
            final ClientStatusViewModel clientStatus,
            final ControllerDataViewModel controllerData
    ) {
        super();

        this.flightPlan = flightPlan;
        this.clientStatus = clientStatus;
        this.controllerData = controllerData;

        setupBindings();
    }

    private void setupBindings() {
//        modelProperty().addListener((observable, oldValue, newValue) -> {
//            flightPlan.setModel(newValue);
//            clientStatus.setModel(newValue);
//            controllerData.setModel(newValue);
//        });
        flightPlan.modelProperty().bind(modelProperty());
        clientStatus.modelProperty().bind(modelProperty());
        controllerData.modelProperty().bind(modelProperty());
    }

    public StringProperty callsignProperty() {
        return stringProperty("callsign", VatsimClient::getCallsign);
    }

    public StringProperty cidProperty() {
        return stringProperty("cid", VatsimClient::getCid);
    }

    public StringProperty realNameProperty() {
        return stringProperty("realName", VatsimClient::getRealName);
    }

    public ObjectProperty<RawClientType> rawClientTypeProperty() {
        return objectProperty("rawClientType", c -> RawClientType.fromString(c.getClientType()));
    }

    public StringProperty frequencyProperty() {
        return stringProperty("frequency", VatsimClient::getFrequency);
    }

    public StringProperty protRevisionProperty() {
        return stringProperty("protRevision", VatsimClient::getProtRevsion);
    }

    public StringProperty ratingProperty() {
        return stringProperty("rating", VatsimClient::getRating);
    }

    public StringProperty transponderProperty() {
        return stringProperty("transponder", VatsimClient::getTransponder);
    }

    public ObjectProperty<RawFacilityType> rawFacilityTypeProperty() {
        return objectProperty("rawFacilityType", c -> RawFacilityType.fromString(c.getFaciliyType()));
    }

    public StringProperty visualRangeProperty() {
        return stringProperty("visualRange", VatsimClient::getVisualRange);
    }

    public StringProperty atisMessageProperty() {
        return stringProperty("atisMessage", VatsimClient::getAtisMessage);
    }

    public ObjectProperty<ZonedDateTime> logonTimeProperty() {
        return objectProperty("logonTime", VatsimClient::getTimeLogon);
    }

    public ObjectProperty<ZonedDateTime> lastAtisTimeProperty() {
        return objectProperty("lastAtisTime", VatsimClient::getTimeLastAtisReceived);
    }

    public StringProperty qnhInchesMercuryProperty() {
        return stringProperty("qnhInchesMercury", VatsimClient::getQnhInchesMercury);
    }

    public StringProperty qnhMillibarsProperty() {
        return stringProperty("qnhMillibars", VatsimClient::getQnhMillibars);
    }

    public ClientStatusViewModel clientStatus() {
        return clientStatus;
    }

    public FlightPlanViewModel flightPlan() {
        return flightPlan;
    }

    public ControllerDataViewModel controllerData() {
        return controllerData;
    }
}
