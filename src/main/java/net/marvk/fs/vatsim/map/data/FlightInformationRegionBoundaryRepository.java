package net.marvk.fs.vatsim.map.data;

import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Rectangle;
import com.github.davidmoten.rtree2.internal.EntryDefault;
import com.google.inject.Inject;
import com.google.inject.Provider;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimAirspace;
import net.marvk.fs.vatsim.api.data.VatsimAirspaceGeneral;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Log4j2
public class FlightInformationRegionBoundaryRepository extends ProviderRepository<FlightInformationRegionBoundary, VatsimAirspace> {
    private final Lookup<FlightInformationRegionBoundary> icao = Lookup.fromProperty(FlightInformationRegionBoundary::getIcao);
    private final FlightInformationRegionRepository flightInformationRegionRepository;
    private final CountryRepository countryRepository;
    private RTree<FlightInformationRegionBoundary, PolygonGeometry> rTree = RTree.create();

    @Inject
    public FlightInformationRegionBoundaryRepository(
            final VatsimApi vatsimApi,
            final Provider<FlightInformationRegionBoundary> provider,
            final FlightInformationRegionRepository flightInformationRegionRepository,
            final CountryRepository countryRepository
    ) {
        super(vatsimApi, provider);
        this.flightInformationRegionRepository = flightInformationRegionRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    protected String keyFromModel(final VatsimAirspace airspace) {
        final VatsimAirspaceGeneral general = airspace.getGeneral();
        return String.join(
                "_",
                general.getIcao(),
                String.valueOf(general.getExtension()),
                String.valueOf(general.getOceanic())
        );
    }

    @Override
    protected String keyFromViewModel(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
        return String.join(
                "_",
                flightInformationRegionBoundary.getIcao(),
                String.valueOf(flightInformationRegionBoundary.isExtension()),
                String.valueOf(flightInformationRegionBoundary.isOceanic())
        );
    }

    @Override
    protected Collection<VatsimAirspace> extractModels(final VatsimApi api) throws VatsimApiException {
        return api.firBoundaries().getAirspaces();
    }

    @Override
    protected void onAdd(final FlightInformationRegionBoundary toAdd, final VatsimAirspace airspace) {
        icao.put(toAdd);

        final Country country = countryRepository.getByPrefix(toAdd.getIcao().substring(0, 2));
        if (country != null) {
            toAdd.countryPropertyWritable().set(country);
        } else {
            log.warn("Could not determine Country for FIRB with ICAO: \"%s\"".formatted(toAdd.getIcao()));
        }
    }

    @Override
    protected void updateList(final Collection<VatsimAirspace> updatedModels) {
        super.updateList(updatedModels);
        mergeExtensions();
        createRTree();
        linkFirs();
    }

    private void linkFirs() {
        for (final FlightInformationRegionBoundary firb : list()) {
            final List<FlightInformationRegion> byIcao = flightInformationRegionRepository.getByIcao(firb.getIcao());

            final List<FlightInformationRegion> firs;
            if (byIcao.size() <= 1) {
                firs = byIcao;
            } else {
                final List<FlightInformationRegion> fss = filterFss(byIcao, true);
                final List<FlightInformationRegion> nonFss = filterFss(byIcao, false);
                if (fss.isEmpty()) {
                    firs = nonFss;
                } else if (nonFss.isEmpty()) {
                    firs = fss;
                } else if (firb.isOceanic()) {
                    firs = fss;
                } else {
                    firs = nonFss;
                }
            }

            firs.forEach(firb.getFlightInformationRegionsWritable()::add);
        }
    }

    private List<FlightInformationRegion> filterFss(final List<FlightInformationRegion> byIcao, final boolean fss) {
        return byIcao.stream()
                     .filter(e -> isRadio(e) == fss)
                     .collect(Collectors.toList());
    }

    private boolean isRadio(final FlightInformationRegion e) {
        return StringUtils.containsIgnoreCase(e.getName(), "radio") || StringUtils.containsIgnoreCase(e.getName(), "oceanic");
    }

    private void createRTree() {
        final List<Entry<FlightInformationRegionBoundary, PolygonGeometry>> entries = list()
                .stream()
                .map(FlightInformationRegionBoundaryRepository::entry)
                .collect(Collectors.toList());

        rTree = RTree.star().create(entries);
    }

    public List<Entry<FlightInformationRegionBoundary, PolygonGeometry>> getByPositionAsEntries(final Point2D position) {
        return getAllByPositionAsStream(position)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<FlightInformationRegionBoundary> getByPosition(final Point2D position) {
        return getAllByPositionAsStream(position)
                .map(Entry::value)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<FlightInformationRegionBoundary> getByPosition(final Point2D position, final double distance) {
        return getAllByPositionAsStream(position, distance)
                .map(Entry::value)
                .distinct()
                .collect(Collectors.toList());
    }

    private Stream<Entry<FlightInformationRegionBoundary, PolygonGeometry>> getAllByPositionAsStream(final Point2D position, final double distance) {
        return Stream.of(
                getByPositionAsStream(position, distance),
                getByPositionAsStream(position.add(360, 0), distance),
                getByPositionAsStream(position.subtract(360, 0), distance)
        ).flatMap(Function.identity());
    }

    private Stream<Entry<FlightInformationRegionBoundary, PolygonGeometry>> getAllByPositionAsStream(final Point2D position) {
        return Stream.of(
                getByPositionAsStream(position),
                getByPositionAsStream(position.add(360, 0)),
                getByPositionAsStream(position.subtract(360, 0))
        ).flatMap(Function.identity());
    }

    private Stream<Entry<FlightInformationRegionBoundary, PolygonGeometry>> getByPositionAsStream(final Point2D position) {
        final var spliterator = rTree.search(Geometries.point(position.getX(), position.getY())).spliterator();
        return StreamSupport.stream(spliterator, false);
    }

    private Stream<Entry<FlightInformationRegionBoundary, PolygonGeometry>> getByPositionAsStream(final Point2D position, final double distance) {
        final var spliterator = rTree.search(Geometries.point(position.getX(), position.getY()), distance)
                                     .spliterator();
        return StreamSupport.stream(spliterator, false);
    }

    private static Entry<FlightInformationRegionBoundary, PolygonGeometry> entry(final FlightInformationRegionBoundary e) {
        return new EntryDefault<>(e, new PolygonGeometry(e.getPolygon()));
    }

    private void mergeExtensions() {
        final List<FlightInformationRegionBoundary> extensions =
                list()
                        .stream()
                        .filter(FlightInformationRegionBoundary::isExtension)
                        .collect(Collectors.toList());

        items.removeAll(extensions);
        icao.removeAll(extensions);

        for (final FlightInformationRegionBoundary extension : extensions) {
            final Optional<FlightInformationRegionBoundary> maybeParent =
                    items.stream()
                         .filter(e -> e.isOceanic() == extension.isOceanic())
                         .filter(e -> e.getIcao().equals(extension.getIcao()))
                         .findFirst();

            if (maybeParent.isPresent()) {
                final FlightInformationRegionBoundary parent = maybeParent.get();

                parent.mergeInto(extension);
            } else {
                log.warn("No parent found for extension FIR " + keyFromViewModel(extension));
            }
        }
    }

    public List<FlightInformationRegionBoundary> getByIcao(final String icao) {
        return this.icao.get(icao);
    }

    public FlightInformationRegionBoundary getByIcao(final String icao, final boolean oceanic, final boolean extension) {
        final List<FlightInformationRegionBoundary> firs = getByIcao(icao);
        final var result = firs
                .stream()
                .filter(e -> icao.equals(e.getIcao()))
                .filter(e -> e.isExtension() == extension)
                .filter(e -> e.isOceanic() == oceanic)
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            return null;
        }

        if (result.size() > 1) {
            log.warn("Duplicate FIRs: " + firs);
        }

        return result.get(0);
    }

    private static class PolygonGeometry implements Geometry {
        private final Polygon polygon;
        private final Rectangle bound;

        public PolygonGeometry(final Polygon polygon) {
            this.polygon = polygon;
            final Rectangle2D b = polygon.boundary();
            this.bound = Geometries.rectangle(b.getMinX(), b.getMinY(), b.getMaxX(), b.getMaxY());
        }

        @Override
        public double distance(final Rectangle r) {
            if (!(r instanceof Point)) {
                throw new UnsupportedOperationException();
            }
            final Point p = (Point) r;
            return polygon.distance(p.x(), p.y());
        }

        @Override
        public Rectangle mbr() {
            return bound;
        }

        @Override
        public boolean intersects(final Rectangle r) {
            if (!(r instanceof Point)) {
                throw new UnsupportedOperationException();
            }
            final Point p = (Point) r;
            return polygon.isInside(p.x(), p.y());
        }

        @Override
        public boolean isDoublePrecision() {
            return true;
        }
    }
}
