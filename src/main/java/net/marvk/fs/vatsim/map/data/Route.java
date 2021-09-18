package net.marvk.fs.vatsim.map.data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Route {
    private final List<String> waypoints;

    private Route(final List<String> waypoints) {
        this.waypoints = waypoints;
    }

    public static Route parse(final String s) {
        return new Route(Arrays.stream(s.split(" ")).collect(Collectors.toUnmodifiableList()));
    }

    public List<String> getWaypoints() {
        return waypoints;
    }
}
