package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.nio.file.Path;

public class ColorSchemeRepository extends FileRepository<ColorScheme> {
    @Inject
    public ColorSchemeRepository(@Named("userConfigDir") final Path path, @Named("colorSchemeSerializer") final Adapter<ColorScheme> adapter) {
        super(path, adapter);
    }

    @Override
    protected String elementDescriptor(final ColorScheme colorScheme) {
        return "%s (%s)".formatted(colorScheme.getUuid(), colorScheme.getName());
    }

    @Override
    protected String singular() {
        return "color scheme";
    }

    @Override
    protected String plural() {
        return "color schemes";
    }

    @Override
    protected String directoryName() {
        return "ColorSchemes";
    }
}
