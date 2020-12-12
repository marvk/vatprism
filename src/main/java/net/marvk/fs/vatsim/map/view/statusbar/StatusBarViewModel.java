package net.marvk.fs.vatsim.map.view.statusbar;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.data.ClientRepository;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.view.StatusScope;

public class StatusBarViewModel implements ViewModel {
    private final ObjectProperty<Point2D> mouseWorldPosition = new SimpleObjectProperty<>();
    private final ObservableList<FlightInformationRegionBoundary> highlightedFirs = FXCollections.observableArrayList();

    private final ReadOnlyObjectWrapper<PlayerStats> playerStats = new ReadOnlyObjectWrapper<>(new PlayerStats());
    private final ClientRepository clientRepository;

    @InjectScope
    private StatusScope statusScope;

    @Inject
    public StatusBarViewModel(final ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void initialize() {
        mouseWorldPosition.bind(statusScope.mouseWorldPositionProperty());

        Bindings.bindContent(highlightedFirs, statusScope.highlightedFirs());
        clientRepository.list().addListener((ListChangeListener<Client>) c -> updatePlayerStats());
        updatePlayerStats();
    }

    private void updatePlayerStats() {
        playerStats.set(PlayerStats.read(clientRepository.list()));
    }

    public ObjectProperty<Point2D> mouseWorldPositionProperty() {
        return mouseWorldPosition;
    }

    public ObservableList<FlightInformationRegionBoundary> getHighlightedFirs() {
        return highlightedFirs;
    }

    public PlayerStats getPlayerStats() {
        return playerStats.get();
    }

    public ReadOnlyObjectProperty<PlayerStats> playerStatsProperty() {
        return playerStats.getReadOnlyProperty();
    }
}
