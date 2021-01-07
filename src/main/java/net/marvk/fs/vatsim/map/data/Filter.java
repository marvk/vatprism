package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.paint.Color;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

@Log4j2
public class Filter implements Predicate<Client> {
    private final ReadOnlyObjectProperty<Type> type;

    private final ReadOnlyStringProperty name;
    private final ReadOnlyObjectProperty<Color> color;

    private final StringPredicateListPredicate callsignPredicates;
    private final ReadOnlyBooleanProperty callsignsOrCids;
    private final StringPredicateListPredicate cidPredicates;

    private final StringPredicateListPredicate departureAirportPredicates;
    private final ReadOnlyBooleanProperty departuresOrArrivals;
    private final StringPredicateListPredicate arrivalAirportPredicates;

    private final EnumSetPredicate<FlightStatus> flightStatuses;
    private final EnumSetPredicate<FlightType> flightTypes;
    private final EnumSetPredicate<FlightRule> flightRules;

    private final ReadOnlyBooleanProperty flightPlanRequired;

    public Filter(
            final String name,
            final Color color,
            final Type type,
            final List<StringPredicate> callsignPredicates,
            final Boolean callsignsOrCids,
            final List<StringPredicate> cidPredicates,
            final List<StringPredicate> departureAirportPredicates,
            final Boolean departuresOrArrivals,
            final List<StringPredicate> arrivalAirportPredicates,
            final Collection<FlightStatus> flightStatuses,
            final Collection<FlightType> flightTypes,
            final Collection<FlightRule> flightRules,
            final boolean flightPlanRequired
    ) {
        this.type = new ImmutableObjectProperty<>(type);
        this.name = new ImmutableStringProperty(name);
        this.color = new ImmutableObjectProperty<>(color);
        this.callsignPredicates = new StringPredicateListPredicate(callsignPredicates);
        this.callsignsOrCids = new ImmutableBooleanProperty(callsignsOrCids);
        this.cidPredicates = new StringPredicateListPredicate(cidPredicates);
        this.departureAirportPredicates = new StringPredicateListPredicate(departureAirportPredicates);
        this.departuresOrArrivals = new ImmutableBooleanProperty(departuresOrArrivals);
        this.arrivalAirportPredicates = new StringPredicateListPredicate(arrivalAirportPredicates);
        this.flightStatuses = new EnumSetPredicate<>(EnumSet.copyOf(flightStatuses));
        this.flightTypes = new EnumSetPredicate<>(EnumSet.copyOf(flightTypes));
        this.flightRules = new EnumSetPredicate<>(EnumSet.copyOf(flightRules));
        this.flightPlanRequired = new ImmutableBooleanProperty(flightPlanRequired);
    }

    @Override
    public boolean test(final Client client) {
        final boolean isPilot = client instanceof Pilot;
        if (isPilot && type.get() == Type.CONTROLLER) {
            return false;
        }

        final boolean isController = client instanceof Controller;
        if (isController && type.get() == Type.PILOT) {
            return false;
        }

        if (testCallsignAndCid(client)) {
            return false;
        }

        if (isController) {
            final Controller controller = (Controller) client;

            if (!testAirport(departureAirportPredicates, controller.getWorkingAirport())) {
                return false;
            }

            if (!testFirb(departureAirportPredicates, controller.getWorkingFlightInformationRegionBoundary())) {
                return false;
            }

            // TODO test ratings

            // TODO test facilities
        } else if (isPilot) {
            final Pilot pilot = (Pilot) client;

            if (flightPlanRequired.get()) {
                if (!pilot.getFlightPlan().isSet()) {
                    return false;
                }
            }

            if (!testAirport(departureAirportPredicates, ((Pilot) client).getFlightPlan().getDepartureAirport())) {
                return false;
            }

            if (!testAirport(arrivalAirportPredicates, ((Pilot) client).getFlightPlan().getArrivalAirport())) {
                return false;
            }

            if (testFlightStatus(pilot)) {
                return false;
            }

            if (testFlightTypes(pilot)) {
                return false;
            }

            if (!testFlightRules(pilot)) {
                return false;
            }
        } else {
            throw new AssertionError();
        }

        return false;
    }

    private boolean testFlightStatus(final Pilot pilot) {
        if (pilot.getEta().isArriving()) {
            return !flightStatuses.test(FlightStatus.ARRIVING);
        }
        if (pilot.getEta().isDeparting()) {
            return !flightStatuses.test(FlightStatus.DEPARTING);
        }
        if (pilot.getEta().isUnknown()) {
            return !flightStatuses.test(FlightStatus.UNKNOWN);
        }
        if (pilot.getEta().isEnRoute()) {
            return !flightStatuses.test(FlightStatus.ENROUTE);
        }
        return false;
    }

    private boolean testFlightTypes(final Pilot pilot) {
        if (pilot.getFlightPlan().isDomestic()) {
            return !flightTypes.test(FlightType.DOMESTIC);
        }
        if (pilot.getFlightPlan().isInternational()) {
            return !flightTypes.test(FlightType.INTERNATIONAL);
        }
        return false;
    }

    private boolean testFlightRules(final Pilot pilot) {
        return testFlightRule(pilot, FlightRule.UNKNOWN) &&
                testFlightRule(pilot, FlightRule.IFR) &&
                testFlightRule(pilot, FlightRule.VFR) &&
                testFlightRule(pilot, FlightRule.DVFR) &&
                testFlightRule(pilot, FlightRule.SVFR);
    }

    private boolean testFlightRule(final Pilot pilot, final FlightRule flightRule) {
        if (pilot.getFlightPlan().getFlightRule() == flightRule) {
            return flightRules.test(flightRule);
        }
        return true;
    }

    private boolean testAirport(final Predicate<String> predicate, final Airport airport) {
        return airport != null && (predicate.test(airport.getIcao()) || testFirb(predicate, airport.getFlightInformationRegionBoundary()));
    }

    private boolean testFirb(final Predicate<String> predicate, final FlightInformationRegionBoundary firb) {
        return firb != null && predicate.test(firb.getIcao());
    }

    private boolean testCallsignAndCid(final Client client) {
        final boolean callsignMatches = testCallsign(client);
        final boolean cidMatches = testCid(client);

        if (callsignsOrCids.get()) {
            return !callsignMatches && !cidMatches;
        } else {
            return !callsignMatches || !cidMatches;
        }
    }

    private boolean testCid(final Client client) {
        return cidPredicates.test(client.getCidString());
    }

    private boolean testCallsign(final Client client) {
        return callsignPredicates.test(client.getCallsign());
    }

    public Type getType() {
        return type.get();
    }

    public ReadOnlyObjectProperty<Type> typeProperty() {
        return type;
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public Color getColor() {
        return color.get();
    }

    public ReadOnlyObjectProperty<Color> colorProperty() {
        return color;
    }

    public List<StringPredicate> getCallsignPredicates() {
        return callsignPredicates.predicates;
    }

    public boolean isCallsignsOrCids() {
        return callsignsOrCids.get();
    }

    public ReadOnlyBooleanProperty callsignsOrCidsProperty() {
        return callsignsOrCids;
    }

    public List<StringPredicate> getCidPredicates() {
        return cidPredicates.predicates;
    }

    public List<StringPredicate> getDepartureAirportPredicates() {
        return departureAirportPredicates.predicates;
    }

    public boolean isDeparturesOrArrivals() {
        return departuresOrArrivals.get();
    }

    public ReadOnlyBooleanProperty departuresOrArrivalsProperty() {
        return departuresOrArrivals;
    }

    public List<StringPredicate> getArrivalAirportPredicates() {
        return arrivalAirportPredicates.predicates;
    }

    public Collection<FlightStatus> getFlightStatuses() {
        return flightStatuses.set;
    }

    public Collection<FlightType> getFlightTypes() {
        return flightTypes.set;
    }

    public Collection<FlightRule> getFlightRules() {
        return flightRules.set;
    }

    private static class EnumSetPredicate<E extends Enum<E>> implements Predicate<E> {
        private final EnumSet<E> set;

        public EnumSetPredicate(final EnumSet<E> set) {
            this.set = set;
        }

        @Override
        public boolean test(final E e) {
            return set == null || set.isEmpty() || set.contains(e);
        }
    }

    private static class StringPredicateListPredicate implements Predicate<String> {
        private final List<StringPredicate> predicates;

        public StringPredicateListPredicate(final List<StringPredicate> predicates) {
            this.predicates = predicates;
        }

        @Override
        public boolean test(final String s) {
            if (predicates == null || predicates.isEmpty()) {
                return true;
            }

            for (final StringPredicate predicate : predicates) {
                if (predicate.test(s)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class StringPredicate implements Predicate<String> {
        private final Pattern pattern;
        private final String content;
        private final boolean regex;

        public StringPredicate(final Pattern pattern, final String content, final boolean regex) {
            this.pattern = pattern;
            this.content = content;
            this.regex = regex;
        }

        @Override
        public boolean test(final String s) {
            return pattern.matcher(s).matches();
        }

        public static Optional<StringPredicate> tryCreate(final String content, final boolean regex) {
            return Optional.ofNullable(pattern(content, regex))
                           .map(pattern -> new StringPredicate(pattern, content, regex));
        }

        private static Pattern pattern(final String content, final boolean regex) {
            try {
                return Pattern.compile(toRegexLineString(toRegex(content, regex)), Pattern.CASE_INSENSITIVE);
            } catch (final PatternSyntaxException e) {
                log.trace(() -> "Tried to parse invalid regex pattern %s".formatted(content));
                return null;
            }
        }

        private static String toRegexLineString(final String regexString) {
            return "^%s$".formatted(regexString);
        }

        private static String toRegex(final String pattern, final boolean regex) {
            return regex ? pattern : simplePatternToRegex(pattern);
        }

        private static String simplePatternToRegex(final String simplePattern) {
            return Arrays.stream(simplePattern.split("\\*", -1)).collect(Collectors.joining(".*"));
        }
    }

    public enum Type {
        PILOT,
        CONTROLLER
    }

    public enum FlightStatus {
        DEPARTING,
        ENROUTE,
        ARRIVING,
        UNKNOWN
    }

    public enum FlightType {
        DOMESTIC,
        INTERNATIONAL
    }
}
