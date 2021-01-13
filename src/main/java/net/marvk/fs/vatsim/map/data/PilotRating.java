package net.marvk.fs.vatsim.map.data;

import lombok.Value;
import net.marvk.fs.vatsim.api.data.VatsimPilotRating;

import java.util.HashMap;
import java.util.Map;

@Value
public class PilotRating implements Comparable<PilotRating> {
    private static final Map<String, PilotRating> RATINGS = new HashMap<>();

    int id;
    String shortName;
    String longName;

    public static PilotRating of(final int id, final String shortName, final String longName) {
        return RATINGS.computeIfAbsent(shortName, s -> new PilotRating(id, shortName, longName));
    }

    public static PilotRating of(final VatsimPilotRating rating) {
        return of(Integer.parseInt(rating.getId()), rating.getShortName(), rating.getLongName());
    }

    public static PilotRating[] values() {
        return RATINGS.values().toArray(PilotRating[]::new);
    }

    @Override
    public int compareTo(final PilotRating o) {
        return Integer.compare(id, o.id);
    }
}
