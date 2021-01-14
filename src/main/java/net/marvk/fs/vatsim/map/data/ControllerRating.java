package net.marvk.fs.vatsim.map.data;

import lombok.Value;
import net.marvk.fs.vatsim.api.data.VatsimControllerRating;

import java.util.LinkedHashMap;
import java.util.Map;

@Value
public class ControllerRating implements Comparable<ControllerRating> {
    private static final Map<Integer, ControllerRating> RATINGS = new LinkedHashMap<>();

    int id;
    String shortName;
    String longName;

    public static ControllerRating of(final int id, final String shortName, final String longName) {
        return RATINGS.computeIfAbsent(id, key -> new ControllerRating(id, shortName, longName));
    }

    public static ControllerRating of(final VatsimControllerRating rating) {
        return of(Integer.parseInt(rating.getId()), rating.getShortName(), rating.getLongName());
    }

    public static ControllerRating[] values() {
        return RATINGS.values().toArray(ControllerRating[]::new);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return id == ((ControllerRating) o).id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int compareTo(final ControllerRating o) {
        return Integer.compare(id, o.id);
    }
}
