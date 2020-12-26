package net.marvk.fs.vatsim.map.data;

import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.internal.EntryDefault;
import com.google.inject.Inject;
import com.google.inject.Provider;
import javafx.geometry.Point2D;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimAirport;
import net.marvk.fs.vatsim.map.GeomUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Log4j2
public class AirportRepository extends ProviderRepository<Airport, AirportRepository.VatsimAirportWrapper> {
    private final Lookup<Airport> icaoLookup = Lookup.fromProperty(Airport::getIcao);
    private final Lookup<Airport> iataLookup = Lookup.fromCollection(Airport::getIatas);
    private final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository;
    private RTree<Airport, Point> rTree = RTree.create();

    @Inject
    public AirportRepository(final VatsimApi vatsimApi, final Provider<Airport> provider, final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository) {
        super(vatsimApi, provider);
        this.flightInformationRegionBoundaryRepository = flightInformationRegionBoundaryRepository;
    }

    @Override
    protected String keyFromModel(final VatsimAirportWrapper vatsimAirport) {
        return vatsimAirport.getIcao();
    }

    @Override
    protected String keyFromViewModel(final Airport airport) {
        return airport.getIcao();
    }

    @Override
    protected Collection<VatsimAirportWrapper> extractModelList(final VatsimApi api) throws VatsimApiException {
        return api
                .vatSpy()
                .getAirports()
                .stream()
                .filter(e -> !e.getPseudo())
                .collect(Collectors.groupingBy(VatsimAirport::getIcao))
                .values()
                .stream()
                .map(VatsimAirportWrapper::new)
                .collect(Collectors.toList());
    }

    @Override
    protected void onAdd(final Airport toAdd, final VatsimAirportWrapper vatsimAirport) {
        icaoLookup.put(toAdd);
        iataLookup.put(toAdd);

        findFirb(vatsimAirport.getFir())
                .ifPresent(e -> toAdd.flightInformationRegionBoundaryPropertyWritable().set(e));
    }

    private Optional<FlightInformationRegionBoundary> findFirb(final String icao) {
        final List<FlightInformationRegionBoundary> firbs = flightInformationRegionBoundaryRepository.getByIcao(icao);

        final Optional<FlightInformationRegionBoundary> nonOceanic = firbs
                .stream()
                .filter(e -> !e.isOceanic())
                .findFirst();

        if (nonOceanic.isPresent()) {
            return nonOceanic;
        }

        return firbs.stream().findFirst();
    }

    @Override
    protected void updateList(final Collection<VatsimAirportWrapper> updatedModels) {
        super.updateList(updatedModels);
        createRTree();
    }

    private void createRTree() {
        final List<Entry<Airport, Point>> list = list()
                .stream()
                .filter(e -> e.getPosition().getX() >= -180)
                .filter(e -> e.getPosition().getX() <= 180)
                .filter(e -> e.getPosition().getY() >= -90)
                .filter(e -> e.getPosition().getY() <= 90)
                .map(AirportRepository::entry)
                .collect(Collectors.toList());

        rTree = RTree.star().create(list);
    }

    public List<Airport> searchByPosition(final Point2D p, final double maxDistance, final int maxCount) {
        return streamSearchByPosition(p, maxDistance, maxCount)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Stream<Airport> streamSearchByPosition(final Point2D p, final double maxDistance, final int maxCount) {
        final var spliterator = rTree.nearest(Geometries.point(p.getX(), p.getY()), maxDistance, maxCount)
                                     .spliterator();
        return StreamSupport.stream(spliterator, false)
                            .map(Entry::value);
    }

    private static EntryDefault<Airport, Point> entry(final Airport e) {
        return new EntryDefault<>(e, Geometries.pointGeographic(e.getPosition().getX(), e.getPosition().getY()));
    }

    public List<Airport> getByIcao(final String icao) {
        return icaoLookup.get(icao);
    }

    public List<Airport> getByIata(final String iata) {
        return iataLookup.get(iata);
    }

    public static final class VatsimAirportWrapper {
        private final String icao;
        private final List<String> names;
        private final Point2D position;
        private final List<String> iatas;
        private final String fir;
        private final boolean pseudo;

        public VatsimAirportWrapper(final List<VatsimAirport> airports) {
            this.icao = icao(airports);
            this.names = names(airports);
            this.position = position(airports);
            this.iatas = iatas(airports);
            this.fir = firs(airports);
            this.pseudo = pseudo(airports);
        }

        public String getIcao() {
            return icao;
        }

        public List<String> getNames() {
            return names;
        }

        public Point2D getPosition() {
            return position;
        }

        public List<String> getIatas() {
            return iatas;
        }

        public String getFir() {
            return fir;
        }

        public boolean isPseudo() {
            return pseudo;
        }

        private boolean pseudo(final List<VatsimAirport> airports) {
            final List<Boolean> pseudos = airports
                    .stream()
                    .map(VatsimAirport::getPseudo)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (pseudos.size() != 1) {
                log.info("Airports with matching ICAO \"" + icao + "\" have mismatched pseudos");
            }

            return pseudos.get(0);
        }

        private String firs(final List<VatsimAirport> airports) {
            final List<String> firs = airports
                    .stream()
                    .map(VatsimAirport::getFlightInformationRegion)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (firs.size() != 1) {
                log.info("Airports with matching ICAO \"" + icao + "\" have mismatched firs: " + firs);
            }

            return firs.get(0);
        }

        private List<String> iatas(final List<VatsimAirport> airports) {
            return airports
                    .stream()
                    .map(VatsimAirport::getIataLid)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }

        private String icao(final List<VatsimAirport> airports) {
            final List<String> icaos = airports
                    .stream()
                    .map(VatsimAirport::getIcao)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (icaos.size() != 1) {
                log.warn("Airports passed to VatsimAirportWrapper have mismatched icaos: " + icaos);
            }

            return icaos.get(0);
        }

        private List<String> names(final List<VatsimAirport> airports) {
            final List<String> names = airports
                    .stream()
                    .map(VatsimAirport::getName)
                    .collect(Collectors.toList());

            if (names.size() != 1) {
                log.info("Airports with matching ICAO \"" + icao + "\" have mismatched names: " + names);
            }

            return names;
        }

        private Point2D position(final List<VatsimAirport> airports) {
            final List<Point2D> points = airports
                    .stream()
                    .map(e -> GeomUtil.parsePoint(e.getPosition()))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (points.size() != 1) {
                log.info("Airports with matching ICAO \"" + icao + "\" have mismatched positions: " + points);
            }

            return points.get(0);
        }
    }
}
