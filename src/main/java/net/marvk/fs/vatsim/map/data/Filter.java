package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.paint.Color;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Log4j2
public class Filter implements Predicate<Client>, UniquelyIdentifiable {
    private final ReadOnlyObjectProperty<UUID> uuid;

    private final EnumSetPredicate<Type> types;
    private final ReadOnlyStringProperty name;
    private final ImmutableBooleanProperty enabled;

    private final ReadOnlyObjectProperty<Color> textColor;
    private final ReadOnlyObjectProperty<Color> backgroundColor;

    private final StringPredicateListPredicate callsignPredicates;
    private final ReadOnlyObjectProperty<Operator> callsignsCidsOperator;
    private final StringPredicateListPredicate cidPredicates;

    private final StringPredicateListPredicate departureAirportPredicates;
    private final ReadOnlyObjectProperty<Operator> departuresArrivalsOperator;
    private final StringPredicateListPredicate arrivalAirportPredicates;

    private final CollectionPredicate<ControllerRating> controllerRatings;
    private final CollectionPredicate<PilotRating> pilotRatings;

    private final EnumSetPredicate<ControllerType> controllerTypes;

    private final EnumSetPredicate<FlightStatus> flightStatuses;
    private final EnumSetPredicate<FlightType> flightTypes;
    private final EnumSetPredicate<FlightRule> flightRules;

    private final ReadOnlyBooleanProperty flightPlanRequired;

    public Filter() {
        this(
                UUID.randomUUID(),
                "Unnamed Filter",
                true,
                Color.BLACK,
                Color.hsb(ThreadLocalRandom.current().nextDouble(360), 0.8, 0.8),
                Collections.singletonList(Type.PILOT),
                Collections.emptyList(),
                Operator.OR,
                Collections.emptyList(),
                Collections.emptyList(),
                Operator.OR,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                false
        );
    }

    public Filter(
            final UUID uuid,
            final String name,
            final boolean enabled,
            final Color textColor,
            final Color backgroundColor,
            final List<Type> types,
            final List<StringPredicate> callsignPredicates,
            final Operator callsignsCidsOperator,
            final List<StringPredicate> cidPredicates,
            final List<StringPredicate> departureAirportPredicates,
            final Operator departuresArrivalsOperator,
            final List<StringPredicate> arrivalAirportPredicates,
            final Collection<PilotRating> pilotRatings,
            final Collection<ControllerRating> controllerRatings,
            final Collection<FlightStatus> flightStatuses,
            final Collection<ControllerType> controllerTypes,
            final Collection<FlightType> flightTypes,
            final Collection<FlightRule> flightRules,
            final boolean flightPlanRequired
    ) {
        this.uuid = new ImmutableObjectProperty<>(uuid);
        this.types = new EnumSetPredicate<>(types);
        this.name = new ImmutableStringProperty(name);
        this.enabled = new ImmutableBooleanProperty(enabled);
        this.textColor = new ImmutableObjectProperty<>(textColor);
        this.backgroundColor = new ImmutableObjectProperty<>(backgroundColor);
        this.callsignPredicates = new StringPredicateListPredicate(callsignPredicates);
        this.callsignsCidsOperator = new ImmutableObjectProperty<>(callsignsCidsOperator);
        this.cidPredicates = new StringPredicateListPredicate(cidPredicates);
        this.departureAirportPredicates = new StringPredicateListPredicate(departureAirportPredicates);
        this.departuresArrivalsOperator = new ImmutableObjectProperty<>(departuresArrivalsOperator);
        this.arrivalAirportPredicates = new StringPredicateListPredicate(arrivalAirportPredicates);
        this.pilotRatings = new CollectionPredicate<>(pilotRatings);
        this.controllerRatings = new CollectionPredicate<>(controllerRatings);
        this.flightStatuses = new EnumSetPredicate<>(flightStatuses);
        this.controllerTypes = new EnumSetPredicate<>(controllerTypes);
        this.flightTypes = new EnumSetPredicate<>(flightTypes);
        this.flightRules = new EnumSetPredicate<>(flightRules);
        this.flightPlanRequired = new ImmutableBooleanProperty(flightPlanRequired);
    }

    @Override
    public boolean test(final Client client) {
        if (!isEnabled()) {
            return false;
        }

        final boolean isPilot = client instanceof Pilot;
        if (isPilot && !types.test(Type.PILOT)) {
            return false;
        }

        final boolean isController = client instanceof Controller;
        if (isController && !types.test(Type.CONTROLLER)) {
            return false;
        }

        if (!testCallsignAndCid(client)) {
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

            if (!controllerRatings.test(controller.getRating())) {
                return false;
            }

            if (!controllerTypes.test(controller.getControllerType())) {
                return false;
            }
        } else if (isPilot) {
            final Pilot pilot = (Pilot) client;

            if (flightPlanRequired.get()) {
                if (!pilot.getFlightPlan().isSet()) {
                    return false;
                }
            }

            if (!testDeparturesAndArrivals(pilot)) {
                return false;
            }

            if (!testFlightStatus(pilot)) {
                return false;
            }

            if (!testFlightTypes(pilot)) {
                return false;
            }

            if (!testFlightRules(pilot)) {
                return false;
            }
        } else {
            throw new AssertionError();
        }

        return true;
    }

    private boolean testDeparturesAndArrivals(final Pilot client) {
        final boolean departuresMatch = testAirport(departureAirportPredicates, client.getFlightPlan()
                                                                                      .getDepartureAirport());
        final boolean arrivalsMatch = testAirport(arrivalAirportPredicates, client.getFlightPlan().getArrivalAirport());

        if (departuresArrivalsOperator.get() == Operator.OR && !departureAirportPredicates.matchesAll() && !arrivalAirportPredicates
                .matchesAll()) {
            return departuresMatch || arrivalsMatch;
        } else {
            return departuresMatch && arrivalsMatch;
        }
    }

    private boolean testFlightStatus(final Pilot pilot) {
        return flightStatuses.matchesAll() ||
                testSingleFlightStatus(pilot, Eta.Status.UNKNOWN, FlightStatus.UNKNOWN) ||
                testSingleFlightStatus(pilot, Eta.Status.ARRIVING, FlightStatus.ARRIVING) ||
                testSingleFlightStatus(pilot, Eta.Status.DEPARTING, FlightStatus.DEPARTING) ||
                testSingleFlightStatus(pilot, Eta.Status.GROUND, FlightStatus.GROUND) ||
                testSingleFlightStatus(pilot, Eta.Status.EN_ROUTE, FlightStatus.ENROUTE) ||
                testSingleFlightStatus(pilot, Eta.Status.MID_AIR, FlightStatus.MIDAIR);
    }

    private boolean testSingleFlightStatus(final Pilot pilot, final Eta.Status status, final FlightStatus flightStatus) {
        return pilot.getEta().is(status) && flightStatuses.test(flightStatus);
    }

    private boolean testFlightTypes(final Pilot pilot) {
        if (flightTypes.matchesAll()) {
            return true;
        }
        if (pilot.getFlightPlan().isDomestic()) {
            return flightTypes.test(FlightType.DOMESTIC);
        }
        if (pilot.getFlightPlan().isInternational()) {
            return flightTypes.test(FlightType.INTERNATIONAL);
        }
        return flightTypes.test(FlightType.UNKNOWN);
    }

    private boolean testFlightRules(final Pilot pilot) {
        return flightRules.matchesAll() ||
                testSingleFlightRule(pilot, FlightRule.UNKNOWN) ||
                testSingleFlightRule(pilot, FlightRule.IFR) ||
                testSingleFlightRule(pilot, FlightRule.VFR) ||
                testSingleFlightRule(pilot, FlightRule.DVFR) ||
                testSingleFlightRule(pilot, FlightRule.SVFR);
    }

    private boolean testSingleFlightRule(final Pilot pilot, final FlightRule flightRule) {
        return pilot.getFlightPlan().getFlightRule() == flightRule && flightRules.test(flightRule);
    }

    private boolean testAirport(final StringPredicateListPredicate predicate, final Airport airport) {
        return predicate.matchesAll() ||
                (airport != null && (predicate.test(airport.getIcao()) || testFirb(predicate, airport.getFlightInformationRegionBoundary())));
    }

    private boolean testFirb(final StringPredicateListPredicate predicate, final FlightInformationRegionBoundary firb) {
        return predicate.matchesAll() ||
                firb != null && predicate.test(firb.getIcao());
    }

    private boolean testCallsignAndCid(final Client client) {
        final boolean callsignMatches = testCallsign(client);
        final boolean cidMatches = testCid(client);

        if (callsignsCidsOperator.get() == Operator.OR && !cidPredicates.matchesAll() && !callsignPredicates.matchesAll()) {
            return callsignMatches || cidMatches;
        } else {
            return callsignMatches && cidMatches;
        }
    }

    private boolean testCid(final Client client) {
        return cidPredicates.test(client.getCidString());
    }

    private boolean testCallsign(final Client client) {
        return callsignPredicates.test(client.getCallsign());
    }

    @Override
    public UUID getUuid() {
        return uuid.get();
    }

    public ReadOnlyObjectProperty<UUID> uuidProperty() {
        return uuid;
    }

    public Set<Type> getTypes() {
        return types.set;
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public ImmutableBooleanProperty enabledProperty() {
        return enabled;
    }

    public Color getTextColor() {
        return textColor.get();
    }

    public ReadOnlyObjectProperty<Color> textColorProperty() {
        return textColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor.get();
    }

    public ReadOnlyObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public List<StringPredicate> getCallsignPredicates() {
        return callsignPredicates.predicates;
    }

    public Operator getCallsignsCidsOperator() {
        return callsignsCidsOperator.get();
    }

    public ReadOnlyObjectProperty<Operator> callsignsCidsOperatorProperty() {
        return callsignsCidsOperator;
    }

    public List<StringPredicate> getCidPredicates() {
        return cidPredicates.predicates;
    }

    public List<StringPredicate> getDepartureAirportPredicates() {
        return departureAirportPredicates.predicates;
    }

    public Operator getDeparturesArrivalsOperator() {
        return departuresArrivalsOperator.get();
    }

    public ReadOnlyObjectProperty<Operator> departuresArrivalsOperatorProperty() {
        return departuresArrivalsOperator;
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

    public Collection<ControllerRating> getControllerRatings() {
        return controllerRatings.items;
    }

    public Collection<PilotRating> getPilotRatings() {
        return pilotRatings.items;
    }

    public Collection<ControllerType> getControllerTypes() {
        return controllerTypes.set;
    }

    public boolean isFlightPlanRequired() {
        return flightPlanRequired.get();
    }

    public ReadOnlyBooleanProperty flightPlanRequiredProperty() {
        return flightPlanRequired;
    }

    private static class EnumSetPredicate<E extends Enum<E>> implements Predicate<E> {
        private final Set<E> set;

        public EnumSetPredicate(final Set<E> set) {
            this.set = set;
        }

        public EnumSetPredicate(final Collection<E> collection) {
            this(collection.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(collection));
        }

        public boolean matchesAll() {
            return set == null || set.isEmpty();
        }

        @Override
        public boolean test(final E e) {
            return matchesAll() || set.contains(e);
        }
    }

    private static class StringPredicateListPredicate implements Predicate<String> {
        private final List<StringPredicate> predicates;

        public StringPredicateListPredicate(final List<StringPredicate> predicates) {
            this.predicates = predicates;
        }

        public boolean matchesAll() {
            return predicates == null || predicates.isEmpty();
        }

        @Override
        public boolean test(final String s) {
            return matchesAll() || predicates.stream().anyMatch(predicate -> predicate.test(s));
        }
    }

    private static class CollectionPredicate<T> implements Predicate<T> {
        private final Collection<T> items;

        public CollectionPredicate(final Collection<T> items) {
            this.items = items == null ? Collections.emptyList() : Collections.unmodifiableSet(new LinkedHashSet<>(items));
        }

        @Override
        public boolean test(final T t) {
            if (items == null || items.isEmpty()) {
                return true;
            }

            return items.contains(t);
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
            return String.join(".*", simplePattern.split("\\*", -1));
        }

        public Pattern getPattern() {
            return pattern;
        }

        public String getContent() {
            return content;
        }

        public boolean isRegex() {
            return regex;
        }
    }

    public enum Operator {
        AND, OR;
    }

    public enum Type {
        PILOT,
        CONTROLLER
    }

    public enum FlightStatus {
        UNKNOWN,
        GROUND,
        DEPARTING,
        ARRIVING,
        ENROUTE,
        MIDAIR
    }

    public enum FlightType {
        UNKNOWN,
        DOMESTIC,
        INTERNATIONAL
    }
}
