package net.marvk.fs.vatsim.map.view;

import com.google.inject.Singleton;
import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import net.marvk.fs.vatsim.map.data.*;

@Singleton
public class ToolbarScope implements Scope {
    private final BooleanProperty autoReload = new SimpleBooleanProperty();

    private final BooleanProperty reloadExecutable = new SimpleBooleanProperty();

    private final BooleanProperty reloadRunning = new SimpleBooleanProperty();

    private final ObjectProperty<Throwable> reloadException = new SimpleObjectProperty<>();

    private final ListProperty<Client> filteredClients = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<Airport> filteredAirports = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<FlightInformationRegionBoundary> filteredFirbs = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<UpperInformationRegion> filteredUirs = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ListProperty<Pilot> filteredPilots = new SimpleListProperty<>();
    private final ListProperty<Controller> filteredControllers = new SimpleListProperty<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ToolbarScope() {
        final ObservableList clients = new FilteredList<>(filteredClients, e -> e instanceof Pilot);
        filteredPilots.set(clients);

        final ObservableList controllers = new FilteredList<>(filteredClients, e -> e instanceof Controller);
        filteredControllers.set(controllers);
    }

    public boolean isAutoReload() {
        return autoReload.get();
    }

    public BooleanProperty autoReloadProperty() {
        return autoReload;
    }

    public void setAutoReload(final boolean autoReload) {
        this.autoReload.set(autoReload);
    }

    public boolean isReloadExecutable() {
        return reloadExecutable.get();
    }

    public BooleanProperty reloadExecutableProperty() {
        return reloadExecutable;
    }

    public void setReloadExecutable(final boolean reloadExecutable) {
        this.reloadExecutable.set(reloadExecutable);
    }

    public boolean isReloadRunning() {
        return reloadRunning.get();
    }

    public BooleanProperty reloadRunningProperty() {
        return reloadRunning;
    }

    public void setReloadRunning(final boolean reloadRunning) {
        this.reloadRunning.set(reloadRunning);
    }

    public Throwable getReloadException() {
        return reloadException.get();
    }

    public ObjectProperty<Throwable> reloadExceptionProperty() {
        return reloadException;
    }

    public void setReloadException(final Throwable reloadException) {
        this.reloadException.set(reloadException);
    }

    public ObservableList<Client> filteredClients() {
        return filteredClients.get();
    }

    public ListProperty<Client> filteredClientsProperty() {
        return filteredClients;
    }

    public ObservableList<Pilot> filteredPilots() {
        return filteredPilots.get();
    }

    public ObservableList<Controller> filteredControllers() {
        return filteredControllers.get();
    }

    public ObservableList<Airport> filteredAirports() {
        return filteredAirports.get();
    }

    public ListProperty<Airport> filteredAirportsProperty() {
        return filteredAirports;
    }

    public ObservableList<FlightInformationRegionBoundary> filteredFirbs() {
        return filteredFirbs.get();
    }

    public ListProperty<FlightInformationRegionBoundary> filteredFirbsProperty() {
        return filteredFirbs;
    }

    public ObservableList<UpperInformationRegion> filteredUirs() {
        return filteredUirs.get();
    }

    public ListProperty<UpperInformationRegion> filteredUirsProperty() {
        return filteredUirs;
    }
}
