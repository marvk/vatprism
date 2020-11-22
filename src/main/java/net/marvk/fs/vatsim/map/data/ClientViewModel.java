package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import net.marvk.fs.vatsim.api.data.VatsimClient;

import java.time.ZonedDateTime;

public class ClientViewModel {
    private final StringProperty callsign = new SimpleStringProperty();
    private final StringProperty cid = new SimpleStringProperty();
    private final StringProperty realName = new SimpleStringProperty();
    private final ObjectProperty<ClientType> clientType = new SimpleObjectProperty<>();
    private final StringProperty frequency = new SimpleStringProperty();
    private final StringProperty protRevision = new SimpleStringProperty();
    private final IntegerProperty rating = new SimpleIntegerProperty();
    private final IntegerProperty transponder = new SimpleIntegerProperty();
    private final ObjectProperty<FacilityType> facilityType = new SimpleObjectProperty<>();
    private final IntegerProperty visualRange = new SimpleIntegerProperty();
    private final StringProperty atisMessage = new SimpleStringProperty();
    private final ObjectProperty<ZonedDateTime> logonTime = new SimpleObjectProperty<>();
    private final ObjectProperty<ZonedDateTime> lastAtisTime = new SimpleObjectProperty<>();
    private final DoubleProperty qnhInchesMercury = new SimpleDoubleProperty();
    private final IntegerProperty qnhMillibars = new SimpleIntegerProperty();

    private final ObjectProperty<ClientStatus> clientStatus = new SimpleObjectProperty<>();
    private final ObjectProperty<FlightPlan> flightPlan = new SimpleObjectProperty<>();

    private final ObjectProperty<VatsimClient> vatsimClient = new SimpleObjectProperty<>();

    public static ClientViewModel fromVatsimClient(final VatsimClient vatsimClient) {
        final ClientViewModel result = new ClientViewModel();
        result.importData(vatsimClient);
        return result;
    }

    public void importData(final ClientViewModel other) {
        importData(other.vatsimClient.get());
    }

    public void importData(final VatsimClient vatsimClient) {
        this.vatsimClient.set(vatsimClient);

        this.callsign.set(vatsimClient.getCallsign());
        this.cid.set(vatsimClient.getCid());
        this.realName.set(vatsimClient.getRealName());
        this.clientType.set(ClientType.fromString(vatsimClient.getClientType()));
        this.frequency.set(vatsimClient.getFrequency());
        this.protRevision.set(vatsimClient.getProtRevsion());
        this.rating.set(Integer.parseInt(vatsimClient.getRating()));
        this.transponder.set(Integer.parseInt(vatsimClient.getTransponder()));
        this.facilityType.set(FacilityType.fromString(vatsimClient.getFaciliyType()));
        this.visualRange.set(Integer.parseInt(vatsimClient.getVisualRange()));
        this.atisMessage.set(vatsimClient.getAtisMessage());
        this.logonTime.set(vatsimClient.getTimeLogon());
        this.lastAtisTime.set(vatsimClient.getTimeLastAtisReceived());
        this.qnhInchesMercury.set(Double.parseDouble(vatsimClient.getQnhInchesMercury()));
        this.qnhMillibars.set(Integer.parseInt(vatsimClient.getQnhMillibars()));

        this.flightPlan.set(FlightPlan.fromVatsimClient(vatsimClient));
        this.clientStatus.set(ClientStatus.fromVatsimClient(vatsimClient));
    }

    public String getCallsign() {
        return callsign.get();
    }

    public StringProperty callsignProperty() {
        return callsign;
    }

    public void setCallsign(final String callsign) {
        this.callsign.set(callsign);
    }

    public String getCid() {
        return cid.get();
    }

    public StringProperty cidProperty() {
        return cid;
    }

    public void setCid(final String cid) {
        this.cid.set(cid);
    }

    public String getRealName() {
        return realName.get();
    }

    public StringProperty realNameProperty() {
        return realName;
    }

    public void setRealName(final String realName) {
        this.realName.set(realName);
    }

    public ClientType getClientType() {
        return clientType.get();
    }

    public ObjectProperty<ClientType> clientTypeProperty() {
        return clientType;
    }

    public void setClientType(final ClientType clientType) {
        this.clientType.set(clientType);
    }

    public String getFrequency() {
        return frequency.get();
    }

    public StringProperty frequencyProperty() {
        return frequency;
    }

    public void setFrequency(final String frequency) {
        this.frequency.set(frequency);
    }

    public String getProtRevision() {
        return protRevision.get();
    }

    public StringProperty protRevisionProperty() {
        return protRevision;
    }

    public void setProtRevision(final String protRevision) {
        this.protRevision.set(protRevision);
    }

    public int getRating() {
        return rating.get();
    }

    public IntegerProperty ratingProperty() {
        return rating;
    }

    public void setRating(final int rating) {
        this.rating.set(rating);
    }

    public int getTransponder() {
        return transponder.get();
    }

    public IntegerProperty transponderProperty() {
        return transponder;
    }

    public void setTransponder(final int transponder) {
        this.transponder.set(transponder);
    }

    public FacilityType getFacilityType() {
        return facilityType.get();
    }

    public ObjectProperty<FacilityType> facilityTypeProperty() {
        return facilityType;
    }

    public void setFacilityType(final FacilityType facilityType) {
        this.facilityType.set(facilityType);
    }

    public int getVisualRange() {
        return visualRange.get();
    }

    public IntegerProperty visualRangeProperty() {
        return visualRange;
    }

    public void setVisualRange(final int visualRange) {
        this.visualRange.set(visualRange);
    }

    public String getAtisMessage() {
        return atisMessage.get();
    }

    public StringProperty atisMessageProperty() {
        return atisMessage;
    }

    public void setAtisMessage(final String atisMessage) {
        this.atisMessage.set(atisMessage);
    }

    public ZonedDateTime getLogonTime() {
        return logonTime.get();
    }

    public ObjectProperty<ZonedDateTime> logonTimeProperty() {
        return logonTime;
    }

    public void setLogonTime(final ZonedDateTime logonTime) {
        this.logonTime.set(logonTime);
    }

    public ZonedDateTime getLastAtisTime() {
        return lastAtisTime.get();
    }

    public ObjectProperty<ZonedDateTime> lastAtisTimeProperty() {
        return lastAtisTime;
    }

    public void setLastAtisTime(final ZonedDateTime lastAtisTime) {
        this.lastAtisTime.set(lastAtisTime);
    }

    public double getQnhInchesMercury() {
        return qnhInchesMercury.get();
    }

    public DoubleProperty qnhInchesMercuryProperty() {
        return qnhInchesMercury;
    }

    public void setQnhInchesMercury(final double qnhInchesMercury) {
        this.qnhInchesMercury.set(qnhInchesMercury);
    }

    public int getQnhMillibars() {
        return qnhMillibars.get();
    }

    public IntegerProperty qnhMillibarsProperty() {
        return qnhMillibars;
    }

    public void setQnhMillibars(final int qnhMillibars) {
        this.qnhMillibars.set(qnhMillibars);
    }

    public ClientStatus getClientStatus() {
        return clientStatus.get();
    }

    public ObjectProperty<ClientStatus> clientStatusProperty() {
        return clientStatus;
    }

    public void setClientStatus(final ClientStatus clientStatus) {
        this.clientStatus.set(clientStatus);
    }

    public FlightPlan getFlightPlan() {
        return flightPlan.get();
    }

    public ObjectProperty<FlightPlan> flightPlanProperty() {
        return flightPlan;
    }

    public void setFlightPlan(final FlightPlan flightPlan) {
        this.flightPlan.set(flightPlan);
    }
}
