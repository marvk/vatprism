package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.util.Comparator;

@Log4j2
public class FilterRepository extends FileRepository<Filter> {

    @Inject
    public FilterRepository(@Named("userConfigDir") final Path path, @Named("filterSerializer") final Adapter<Filter> adapter) {
        super(path, adapter);
    }

    @Override
    protected Comparator<Filter> comparator() {
        return Comparator.comparing(Filter::getName);
    }

    @Override
    protected String elementDescriptor(final Filter filter) {
        return "%s (%s)".formatted(filter.getUuid(), filter.getName());
    }

    @Override
    protected String singular() {
        return "filter";
    }

    @Override
    protected String plural() {
        return "filters";
    }

    @Override
    protected String directoryName() {
        return "Filters";
    }
}
