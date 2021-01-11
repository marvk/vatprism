package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DependencyRepository implements ReadOnlyRepository<Dependency> {
    private static final Pattern LINE_PATTERN =
            Pattern.compile("^\\s*(?:\\('?+(?<licenseName>([^()]|(\\(.*\\)))+?)'?\\)\\s*)+(?<projectName>.*) \\((?<groupId>\\S*):(?<artifactId>\\S*):(?<version>\\S*) - (?:(?<projectUrl>\\S*)|(no url defined))\\)$");

    private final String licenseFileName;

    private ReadOnlyListProperty<Dependency> dependencies;

    @Inject
    public DependencyRepository(@Named("licenseFileName") final String licenseFileName) {
        this.licenseFileName = licenseFileName;
    }

    @Override
    public ObservableList<Dependency> list() {
        if (dependencies == null) {
            dependencies = loadDependencies();
        }

        return dependencies;
    }

    private ReadOnlyListProperty<Dependency> loadDependencies() {
        final InputStream resource = getClass().getResourceAsStream(licenseFileName);

        final Stream<Dependency> result =
                new BufferedReader(new InputStreamReader(resource))
                        .lines()
                        .map(DependencyRepository::parseLine)
                        .filter(Optional::isPresent)
                        .map(Optional::get);

        final Stream<Dependency> additionalDependencies = List.of(
                new Dependency(
                        "Unknown license",
                        "VAT-Spy Data Project",
                        null,
                        null,
                        null,
                        "https://github.com/vatsimnetwork/vatspy-data-project"
                ),
                new Dependency(
                        "Unknown license",
                        "VATSIM API",
                        null,
                        null,
                        null,
                        "https://api.vatsim.net/api/"
                )
        ).stream();

        return new ImmutableListProperty<>(
                Stream.concat(result, additionalDependencies)
                      .sorted(Comparator.comparing(Dependency::getProjectName))
                      .collect(Collectors.toUnmodifiableList())
        );
    }

    public static Optional<Dependency> parseLine(final String line) {
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

    @Override
    public Dependency getByKey(final String key) {
        return list()
                .filtered(e -> key.equals(e.getArtifactId()))
                .stream()
                .findFirst()
                .orElse(null);
    }
}
