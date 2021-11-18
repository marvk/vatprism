package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.beans.property.ReadOnlyListProperty;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Log4j2
public class AirlineRepository extends FileLineRepository<Airline> {
    private static final Pattern AIRLINE_PATTERN = Pattern.compile("^(?<id>-?\\d+),\"?(?<name>.*?)\"?,\"?(?<alias>.*?)\"?,\"?(?<iata>.*?)\"?,\"?(?<icao>.*?)\"?,\"?(?<callsign>.*?)\"?,\"?(?<country>.*?)\"?,\"?(?<active>.*?)\"?$");
    private Map<String, Airline> lookup = new HashMap<>();

    @Inject
    public AirlineRepository(@Named("airlineFileName") final String airlineFileName) {
        super(airlineFileName);
    }

    @Override
    public String extractKey(final Airline airline) {
        return airline.getIcao();
    }

    @Override
    protected ReadOnlyListProperty<Airline> load() {
        final ReadOnlyListProperty<Airline> result = super.load();
        for (final Airline airline : result) {
            final String key = extractKey(airline);
            lookup.compute(key, (unused, oldValue) -> {
                if (oldValue == null) {
                    return airline;
                }
                if (!oldValue.isActive()) {
                    return airline;
                }
                if (!airline.isActive()) {
                    return oldValue;
                }
                log.warn("Duplicate Airline \"%s\"".formatted(key));
                return oldValue;
            });
        }
        return result;
    }

    @Override
    protected Stream<Airline> preSave(final Stream<Airline> stream) {
        return super.preSave(stream).filter(e -> e.getIcao() != null).filter(e -> e.getAirlineId() >= 0);
    }

    @Override
    protected Optional<Airline> parseLine(final String line) {
        final Matcher matcher = AIRLINE_PATTERN.matcher(line);
        if (matcher.matches()) {
            return Optional.of(new Airline(
                    Integer.parseInt(parse(matcher, "id")),
                    parse(matcher, "name"),
                    parse(matcher, "alias"),
                    parse(matcher, "iata"),
                    parse(matcher, "icao"),
                    parse(matcher, "callsign"),
                    parse(matcher, "country"),
                    parse(matcher, "active") != null && !"N".equalsIgnoreCase(parse(matcher, "active"))
            ));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Airline getByKey(final String key) {
        return lookup.get(key);
    }

    private String parse(final Matcher matcher, final String groupName) {
        final String result = matcher.group(groupName);

        if (result == null || result.isBlank() || "\\N".equalsIgnoreCase(result)) {
            return null;
        }

        return result;
    }
}
