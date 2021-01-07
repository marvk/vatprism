package net.marvk.fs.vatsim.map.data;

import lombok.Value;
import net.marvk.fs.vatsim.api.data.VatsimControllerRating;

import java.util.HashMap;
import java.util.Map;

@Value
public class Rating implements Comparable<Rating> {
    private static final Map<String, Rating> RATINGS = new HashMap<>();

    int id;
    String shortName;
    String longName;

    public static Rating of(final int id, final String shortName, final String longName) {
        return RATINGS.computeIfAbsent(shortName, s -> new Rating(id, shortName, longName));
    }

    public static Rating of(final VatsimControllerRating rating) {
        return of(Integer.parseInt(rating.getId()), rating.getShortName(), rating.getLongName());
    }

    @Override
    public int compareTo(final Rating o) {
        return Integer.compare(id, o.id);
    }
}
