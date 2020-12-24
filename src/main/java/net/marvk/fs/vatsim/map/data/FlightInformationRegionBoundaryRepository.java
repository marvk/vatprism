package net.marvk.fs.vatsim.map.data;

import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Rectangle;
import com.github.davidmoten.rtree2.internal.EntryDefault;
import com.google.inject.Inject;
import com.google.inject.Provider;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimAirspace;
import net.marvk.fs.vatsim.api.data.VatsimAirspaceGeneral;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
public class FlightInformationRegionBoundaryRepository extends ProviderRepository<FlightInformationRegionBoundary, VatsimAirspace> {
    private final Lookup<FlightInformationRegionBoundary> icao = Lookup.fromProperty(FlightInformationRegionBoundary::getIcao);
    private RTree<FlightInformationRegionBoundary, Rectangle> rTree = RTree.create();

    @Inject
    public FlightInformationRegionBoundaryRepository(final VatsimApi vatsimApi, final Provider<FlightInformationRegionBoundary> provider) {
        super(vatsimApi, provider);
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
    protected Collection<VatsimAirspace> extractModelList(final VatsimApi api) throws VatsimApiException {
        return api.firBoundaries().getAirspaces();
    }

    @Override
    protected void onAdd(final FlightInformationRegionBoundary toAdd, final VatsimAirspace airspace) {
        icao.put(toAdd);
    }

    @Override
    protected void updateList(final Collection<VatsimAirspace> updatedModels) {
        super.updateList(updatedModels);
        mergeExtensions();
        createRTree();
    }

    private void createRTree() {
        final List<Entry<FlightInformationRegionBoundary, Rectangle>> entries = list()
                .stream()
                .map(FlightInformationRegionBoundaryRepository::entry)
                .collect(Collectors.toList());

        rTree = RTree.star().create(entries);
    }

    public List<Entry<FlightInformationRegionBoundary, Rectangle>> getByPositionAsEntries(final Point2D position) {
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

    private Stream<Entry<FlightInformationRegionBoundary, Rectangle>> getAllByPositionAsStream(final Point2D position) {
        return Stream.concat(
                getByPositionAsStream(position),
                getByPositionAsStream(position.add(360, 0))
        );
    }

    private Stream<Entry<FlightInformationRegionBoundary, Rectangle>> getByPositionAsStream(final Point2D position) {
        final var spliterator = rTree.search(Geometries.point(position.getX(), position.getY())).spliterator();
        return StreamSupport.stream(spliterator, false);
    }

    private static Entry<FlightInformationRegionBoundary, Rectangle> entry(final FlightInformationRegionBoundary e) {
        final Rectangle2D b = e.getPolygon().boundary();
        return new EntryDefault<>(e, Geometries.rectangle(b.getMinX(), b.getMinY(), b.getMaxX(), b.getMaxY()));
    }

    private void mergeExtensions() {
        final List<FlightInformationRegionBoundary> extensions =
                list()
                        .stream()
                        .filter(FlightInformationRegionBoundary::isExtension)
                        .collect(Collectors.toList());

        items.removeAll(extensions);

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

}
