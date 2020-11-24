package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.api.data.VatsimUpperInformationRegion;
import net.marvk.fs.vatsim.map.repository.FlightInformationRegionBoundaryRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class UpperInformationRegionViewModel extends SimpleDataViewModel<VatsimUpperInformationRegion, UpperInformationRegionViewModel> {
    private final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository;

    private final ObservableList<FlightInformationRegionBoundaryViewModel> flightInformationRegionBoundaries =
            FXCollections.observableArrayList();

    @Inject
    public UpperInformationRegionViewModel(final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository) {
        this.flightInformationRegionBoundaryRepository = flightInformationRegionBoundaryRepository;

        setupBindings();
    }

    public ObservableList<FlightInformationRegionBoundaryViewModel> flightInformationRegionBoundaries() {
        return flightInformationRegionBoundaries;
    }

    public StringProperty icaoProperty() {
        return stringProperty("icao", VatsimUpperInformationRegion::getIcao);
    }

    public StringProperty nameProperty() {
        return stringProperty("name", VatsimUpperInformationRegion::getName);
    }

    private void setupBindings() {
        modelProperty().addListener((observable, oldValue, newValue) -> updateFirs(newValue));
    }

    private void updateFirs(final VatsimUpperInformationRegion newValue) {
        if (newValue == null) {
            flightInformationRegionBoundaries.clear();
            return;
        }

        final List<FlightInformationRegionBoundaryViewModel> firbs = newValue
                .getSubordinateFlightInformationRegions()
                .stream()
                .map(flightInformationRegionBoundaryRepository::getByIcao)
//                .peek(e -> {
//                    if (e.size() > 1) {
//                        log.warn("More than 1");
//                    } else {
//                        log.info("1");
//                    }
//                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

//        final String s = firbs
//                .stream()
//                .map(FlightInformationRegionBoundaryViewModel::icaoProperty)
//                .map(ObservableObjectValue::get)
//                .firbs(Collectors.joining(", "));
//        log.info(s);

        flightInformationRegionBoundaries.addAll(firbs);
    }
}
