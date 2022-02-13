package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import javafx.geometry.Point2D;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.api.data.VatsimClientType;
import net.marvk.fs.vatsim.api.data.VatsimController;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Log4j2
public class CallsignParser {
    private static final Pattern UNDERSCORES = Pattern.compile("_+");
    private final AirportRepository airportRepository;
    private final FlightInformationRegionRepository flightInformationRegionRepository;
    private final UpperInformationRegionRepository upperInformationRegionRepository;

    @Inject
    public CallsignParser(
            final AirportRepository airportRepository,
            final FlightInformationRegionRepository flightInformationRegionRepository,
            final UpperInformationRegionRepository upperInformationRegionRepository
    ) {
        this.airportRepository = airportRepository;
        this.flightInformationRegionRepository = flightInformationRegionRepository;
        this.upperInformationRegionRepository = upperInformationRegionRepository;
    }

    public Result parse(final VatsimController controller) {
        if (controller == null ||
                controller.getCallsign() == null ||
                controller.getClientType() == null ||
                controller.getClientType() == VatsimClientType.PILOT
        ) {
            return Result.EMPTY;
        }

//        final String callsign = "LON_N_CTR";
        final String callsign = controller.getCallsign();
        final String cid = controller.getCid();

        final String[] sections = UNDERSCORES.split(callsign);
        final int n = sections.length;

        final ControllerType defaultControllerType = ControllerType.OBS;

        final String identifier;
        final String infix;
        final ControllerType controllerType;

        if (n <= 1) {
            identifier = null;
            infix = null;
            controllerType = defaultControllerType;
        } else if (n == 2) {
            identifier = sections[0];
            infix = null;
            final String s1 = sections[1];
            controllerType = ControllerType.fromString(s1, defaultControllerType);
        } else if (n == 3) {
            identifier = sections[0];
            infix = sections[1];
            controllerType = ControllerType.fromString(sections[2], defaultControllerType);
        } else {
            log.warn("Unexpected callsign " + callsign);
            return Result.EMPTY;
        }

//        System.out.println("identifier = " + identifier);
//        System.out.println("infix = " + infix);
//        System.out.println("controllerType = " + controllerType);

        Airport airport = null;
        UpperInformationRegion uir = null;
        FirResult fir = FirResult.EMPTY;

        if (controllerType != ControllerType.OBS) {
            if (controllerType != ControllerType.CTR && controllerType != ControllerType.FSS) {
                airport = getAirport(controller, identifier);

                if (airport == null) {
                    log.warn(
                            "Could not determine airport \"%s\" for controller with callsign: %s, cid: %s, name: %s, type: %s"
                                    .formatted(identifier, callsign, cid, controller.getName(), controllerType)
                    );
                }
            } else {
                uir = getUir(controller, identifier);

                if (uir == null) {
                    fir = getFir(controller, identifier, infix, controllerType);

                    if (fir.isEmpty()) {
                        log.warn(
                                "Could not determine FIR/UIR \"%s\" for controller with callsign: %s, cid: %s, name: %s, type: %s"
                                        .formatted(identifier, callsign, cid, controller.getName(), controllerType)
                        );
                    }
                } else {
                    fir = FirResult.EMPTY;
                }
            }
        }

        final Result result = new Result(
                controllerType,
                airport,
                fir.flightInformationRegionBoundary,
                fir.flightInformationRegion,
                uir
        );

//        System.out.println(result);


        return result;
    }

    private FirResult getFir(final VatsimController controller, final String identifier, final String infix, final ControllerType controllerType) {
        final List<FlightInformationRegion> firs = flightInformationRegionRepository.getByIdentifierAndInfix(identifier, infix);

        if (firs.isEmpty()) {
            return FirResult.EMPTY;
        }

        if (controllerType == ControllerType.FSS) {
            final Optional<FlightInformationRegion> maybeOceanic = firs
                    .stream()
                    .filter(e -> !e.oceanicBoundaries().isEmpty())
                    .findFirst();

            if (maybeOceanic.isPresent()) {
                return new FirResult(maybeOceanic.get(), maybeOceanic.get().oceanicBoundaries().get(0));
            }
        }

        if (firs.size() > 1) {
            if (firs.stream().map(FlightInformationRegion::getName).distinct().count() > 1) {
                log.warn(
                        "Could not determine exact FIR \"%s\" for controller with callsign: %s, cid: %s, name: %s, type: %s; found %s"
                                .formatted(identifier, controller.getCallsign(), controller.getCid(), controller.getName(), controllerType, firs)
                );
            }
        }

        return firs
                .get(0)
                .boundaries()
                .stream()
                .filter(e -> !e.isOceanic())
                .findFirst()
                .map(e -> new FirResult(firs.get(0), e))
                .orElse(FirResult.EMPTY);
    }

    private UpperInformationRegion getUir(final VatsimClient vatsimClient, final String identifier) {
        return returnFirst(
                () -> extractViewModel(
                        upperInformationRegionRepository.getByIcao(identifier),
                        e -> new Point2D(0., 0.),
                        vatsimClient
                )
        );
    }

    private Airport getAirport(final VatsimClient vatsimClient, final String identifier) {
        return returnFirst(
                () -> extractViewModel(
                        airportRepository.getByIcao(identifier),
                        e -> e.positionProperty().get(),
                        vatsimClient
                ),
                () -> extractViewModel(
                        airportRepository.getByIata(identifier),
                        e -> e.positionProperty().get(),
                        vatsimClient
                )
        );
    }

    @SafeVarargs
    private static <ViewModel> ViewModel returnFirst(final Supplier<ViewModel>... supplier) {
        return Arrays.stream(supplier).map(Supplier::get).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private static <ViewModel> ViewModel extractViewModel(final List<ViewModel> viewModels, final Function<ViewModel, Point2D> positionExtractor, final VatsimClient vatsimClient) {
        if (viewModels.isEmpty()) {
            return null;
        }

        if (viewModels.size() > 1) {
            log.warn("Multiple models " + viewModels);
        }

        return viewModels.get(0);
    }

    @Value
    public static class Result {
        public static final Result EMPTY = new Result(ControllerType.NONE, null, null, null, null);

        ControllerType controllerType;
        Airport airport;
        FlightInformationRegionBoundary flightInformationRegionBoundary;
        FlightInformationRegion flightInformationRegion;
        UpperInformationRegion upperInformationRegion;

        public boolean isEmpty() {
            return EMPTY.equals(this);
        }
    }

    @Value
    private static class FirResult {
        public static final FirResult EMPTY = new FirResult(null, null);

        FlightInformationRegion flightInformationRegion;
        FlightInformationRegionBoundary flightInformationRegionBoundary;

        public boolean isEmpty() {
            return EMPTY.equals(this);
        }
    }
}


