package net.marvk.fs.vatsim.map.view.datadetail.metardetail;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.Metar;
import net.marvk.fs.vatsim.map.data.MetarService;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DetailSubViewModel;

public class MetarDetailViewModel extends DetailSubViewModel<Metar> {
    private final ObjectProperty<Airport> airport = new SimpleObjectProperty<>();
    private final MetarService metarService;
    private final DelegateCommand fetchMetar;

    @Inject
    public MetarDetailViewModel(final MetarService metarService) {
        this.metarService = metarService;
        airport.addListener((observable, oldValue, newValue) -> airportChanged(newValue));
        fetchMetar = new DelegateCommand(() -> new Action() {
            @Override
            protected void action() throws Exception {
                setData(metarService.latestMetar(airport.get()).orElse(null));
            }
        }, airport.isNotNull(), true);
    }

    private void airportChanged(final Airport newValue) {
        if (newValue == null) {
            setData(null);
        } else {
            setData(metarService.lastMetar(newValue).orElse(null));
        }
    }

    public Airport getAirport() {
        return airport.get();
    }

    public ObjectProperty<Airport> airportProperty() {
        return airport;
    }

    public void setAirport(final Airport airport) {
        this.airport.set(airport);
    }

    public DelegateCommand fetchMetar() {
        return fetchMetar;
    }
}
