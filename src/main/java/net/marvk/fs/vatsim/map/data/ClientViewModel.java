package net.marvk.fs.vatsim.map.data;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import net.marvk.fs.vatsim.api.data.VatsimClient;

import java.time.ZonedDateTime;

public class ClientViewModel extends SimpleDataViewModel<VatsimClient> implements ViewModel {
    public ClientViewModel(final DataViewModel<VatsimClient> viewModel) {
        super(viewModel);
    }

    public ClientViewModel(final VatsimClient vatsimClient) {
        super(vatsimClient);
    }

    public ClientViewModel() {
        super();
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

    public ObjectProperty<ClientType> clientTypeProperty() {
        return objectProperty("clientType", c -> ClientType.fromString(c.getClientType()));
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

    public ObjectProperty<FacilityType> facilityTypeProperty() {
        return objectProperty("facilityType", c -> FacilityType.fromString(c.getFaciliyType()));
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

    public ObjectProperty<ClientStatus> clientStatusProperty() {
        return objectProperty("clientStatus", ClientStatus::fromVatsimClient);
    }

    public ObjectProperty<FlightPlan> flightPlanProperty() {
        return objectProperty("flightPlane", FlightPlan::fromVatsimClient);
    }
}
