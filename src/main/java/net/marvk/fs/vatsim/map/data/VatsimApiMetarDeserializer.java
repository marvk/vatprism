package net.marvk.fs.vatsim.map.data;

import java.time.LocalDateTime;

public class VatsimApiMetarDeserializer implements MetarDeserializer {
    @Override
    public Metar deserialize(final String s) {
        return new Metar(s, LocalDateTime.now());
    }
}
