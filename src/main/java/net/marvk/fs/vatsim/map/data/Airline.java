package net.marvk.fs.vatsim.map.data;

import lombok.Value;

import java.util.Comparator;

@Value
public class Airline {
    String icao;

    public static Comparator<Airline> comparingByIcao() {
        return Comparator.comparing(Airline::getIcao);
    }
}
