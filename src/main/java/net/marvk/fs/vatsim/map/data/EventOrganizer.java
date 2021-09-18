package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import net.marvk.fs.vatsim.api.data.VatsimEvent;

public class EventOrganizer implements Settable<VatsimEvent.Organizer> {
    private final ReadOnlyStringWrapper region = new ReadOnlyStringWrapper();

    public String getRegion() {
        return region.get();
    }

    public ReadOnlyStringProperty regionProperty() {
        return region.getReadOnlyProperty();
    }

    private final ReadOnlyStringWrapper division = new ReadOnlyStringWrapper();

    public String getDivision() {
        return division.get();
    }

    public ReadOnlyStringProperty divisionProperty() {
        return division.getReadOnlyProperty();
    }

    private final ReadOnlyStringWrapper subdivision = new ReadOnlyStringWrapper();

    public String getSubdivision() {
        return subdivision.get();
    }

    public ReadOnlyStringProperty subdivisionProperty() {
        return subdivision.getReadOnlyProperty();
    }

    private final ReadOnlyBooleanWrapper organizedByVatsim = new ReadOnlyBooleanWrapper();

    public boolean isOrganizedByVatsim() {
        return organizedByVatsim.get();
    }

    public ReadOnlyBooleanProperty organizedByVatsimProperty() {
        return organizedByVatsim.getReadOnlyProperty();
    }

    public EventOrganizer() {
    }

    public EventOrganizer(final VatsimEvent.Organizer model) {
        setFromModel(model);
    }

    @Override
    public void setFromModel(final VatsimEvent.Organizer model) {
        region.set(model.getRegion());
        division.set(model.getDivision());
        subdivision.set(model.getSubdivision());
        organizedByVatsim.set(model.isOrganizedByVatsim());
    }
}
