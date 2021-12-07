package net.marvk.fs.vatsim.map.view.datadetail;

import net.marvk.fs.vatsim.map.data.*;

import java.util.Objects;
import java.util.ResourceBundle;

public class NameVisitor extends DefaultingDataVisitor<String> {
    private final ResourceBundle resourceBundle;

    public NameVisitor(final ResourceBundle resourceBundle) {
        super(resourceBundle.getString("detail.name.unknown_data_placeholder"));
        this.resourceBundle = Objects.requireNonNull(resourceBundle);
    }

    @Override
    public String visit(final Controller controller) {
        return resourceBundle.getString("detail.name.controller");
    }

    @Override
    public String visit(final UpperInformationRegion upperInformationRegion) {
        return resourceBundle.getString("detail.name.uir");
    }

    @Override
    public String visit(final Airport airport) {
        return resourceBundle.getString("detail.name.airport");
    }

    @Override
    public String visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
        return resourceBundle.getString("detail.name.fir");
    }

    @Override
    public String visit(final FlightInformationRegion flightInformationRegion) {
        return resourceBundle.getString("detail.name.fir");
    }

    @Override
    public String visit(final Pilot pilot) {
        return resourceBundle.getString("detail.name.flight");
    }
}
