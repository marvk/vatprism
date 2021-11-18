package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DependencyRepository extends FileLineRepository<Dependency> {
    private static final Pattern LINE_PATTERN =
            Pattern.compile("^\\s*(?:\\('?+(?<licenseName>([^()]|(\\(.*\\)))+?)'?\\)\\s*)+(?<projectName>.*) \\((?<groupId>\\S*):(?<artifactId>\\S*):(?<version>\\S*) - (?:(?<projectUrl>\\S*)|(no url defined))\\)$");

    @Inject
    public DependencyRepository(@Named("licenseFileName") final String licenseFileName) {
        super(licenseFileName);
    }

    @Override
    protected Stream<Dependency> preSave(final Stream<Dependency> stream) {
        return stream.sorted((d1, d2) -> d1.getProjectName().compareToIgnoreCase(d2.getProjectName()));
    }

    @Override
    public String extractKey(final Dependency dependency) {
        return dependency.getArtifactId();
    }

    @Override
    protected Stream<Dependency> additionalViewModels() {
        return Stream.of(
                new Dependency(
                        "Creative Commons Attribution Share Alike 4.0 International",
                        "VAT-Spy Data Project",
                        null,
                        null,
                        null,
                        "https://github.com/vatsimnetwork/vatspy-data-project"
                ),
                new Dependency(
                        "Restricted",
                        "VATSIM API",
                        null,
                        null,
                        null,
                        "https://api.vatsim.net/api/"
                ),
                new Dependency(
                        "Eclipse Public License v2.0",
                        "B612 Fontface",
                        null,
                        null,
                        null,
                        "https://b612-font.com/"
                ),
                new Dependency(
                        "Creative Commons Attribution-NonCommercial 3.0 Unported",
                        "JetBrains Code2Art",
                        null,
                        null,
                        null,
                        "https://code2art.jetbrains.com/"
                ),
                new Dependency(
                        "Creative Commons Attribution-NonCommercial 3.0 Unported",
                        "OpenFlights Airport Data",
                        null,
                        null,
                        null,
                        "https://code2art.jetbrains.com/"
                )
        );
    }

    @Override
    protected Optional<Dependency> parseLine(final String line) {
        final Matcher matcher = LINE_PATTERN.matcher(line);

        if (matcher.matches()) {
            return Optional.of(new Dependency(
                    matcher.group("licenseName").split("\\) \\(")[0],
                    matcher.group("projectName"),
                    matcher.group("groupId"),
                    matcher.group("artifactId"),
                    matcher.group("version"),
                    matcher.group("projectUrl")
            ));
        } else {
            return Optional.empty();
        }
    }
}
