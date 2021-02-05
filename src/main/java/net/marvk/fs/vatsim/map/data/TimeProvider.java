package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyObjectProperty;

import java.time.ZonedDateTime;

public interface TimeProvider {
    ZonedDateTime getCurrentTime();

    ReadOnlyObjectProperty<ZonedDateTime> currentTimeProperty();
}
