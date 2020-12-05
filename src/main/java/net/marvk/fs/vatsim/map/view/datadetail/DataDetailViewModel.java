package net.marvk.fs.vatsim.map.view.datadetail;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.beans.property.*;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.History;

import java.util.Objects;

public class DataDetailViewModel implements ViewModel {
    private final ObjectProperty<Data> data = new SimpleObjectProperty<>();

    private final ReadOnlyBooleanWrapper historyBackAvailable = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper historyForwardAvailable = new ReadOnlyBooleanWrapper();

    private final ReadOnlyStringWrapper historyBackIdentifier = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper historyForwardIdentifier = new ReadOnlyStringWrapper();

    private final History<Data> history = new History<>();

    private final DataToStringVisitor dataToStringVisitor = new DataToStringVisitor();

    @Inject
    public DataDetailViewModel(final NotificationCenter notificationCenter) {
        data.addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(history.current(), newValue)) {
                history.clear();
                history.add(newValue);
            }
            updateAvailableHistory();
        });

        notificationCenter.subscribe("SET_DATA_DETAIL", (key, payload) -> {
            if (payload.length > 0) {
                if (payload[0] instanceof Data) {
                    addData((Data) payload[0]);
                }
            }
        });
    }

    private void updateAvailableHistory() {
        historyBackAvailable.set(history.previousAvailable());
        historyForwardAvailable.set(history.nextAvailable());
        historyBackIdentifier.set(dataToStringVisitor.visitNullSafe(history.peekPrevious()).orElse(""));
        historyForwardIdentifier.set(dataToStringVisitor.visitNullSafe(history.peekNext()).orElse(""));
    }

    public Data getData() {
        return data.get();
    }

    public ObjectProperty<Data> dataProperty() {
        return data;
    }

    public void setData(final Data data) {
        this.data.set(data);
    }

    public void historyBack() {
        data.set(history.previous());
    }

    public void historyForward() {
        data.set(history.next());
    }

    public ReadOnlyBooleanProperty historyBackAvailableProperty() {
        return historyBackAvailable.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty historyForwardAvailableProperty() {
        return historyForwardAvailable.getReadOnlyProperty();
    }

    public void addData(final Data data) {
        history.add(data);
        this.data.set(data);
    }

    public String getHistoryBackIdentifier() {
        return historyBackIdentifier.get();
    }

    public ReadOnlyStringProperty historyBackIdentifierProperty() {
        return historyBackIdentifier.getReadOnlyProperty();
    }

    public String getHistoryForwardIdentifier() {
        return historyForwardIdentifier.get();
    }

    public ReadOnlyStringProperty historyForwardIdentifierProperty() {
        return historyForwardIdentifier.getReadOnlyProperty();
    }

    private static class DataToStringVisitor implements DataVisitor<String> {
        @Override
        public String visit(final Airport airport) {
            return airport.getIcao();
        }

        @Override
        public String visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
            return flightInformationRegionBoundary.getIcao();
        }

        @Override
        public String visit(final Pilot pilot) {
            return pilot.getCallsign();
        }
    }
}
