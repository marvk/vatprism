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

public class DependencyRepository implements ReadOnlyRepository<Dependency> {
    private static final Pattern LINE_PATTERN =
            Pattern.compile("^\\s*(?:\\('?(?<licenseName>[^()]*)'?\\) )+(?<projectName>\\S*) \\((?<groupId>\\S*):(?<artifactId>\\S*):(?<version>\\S*) - (?<projectUrl>\\S*)\\)$");

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

        final List<Dependency> result =
                new BufferedReader(new InputStreamReader(resource))
                        .lines()
                        .map(DependencyRepository::parseLine)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .sorted(Comparator.comparing(Dependency::getProjectName))
                        .collect(Collectors.toList());

        return new ImmutableListProperty<>(result);
    }

    public static Optional<Dependency> parseLine(final String line) {
        final Matcher matcher = LINE_PATTERN.matcher(line);

        if (matcher.matches()) {
            System.out.println("line = >>>>>%s<<<<<".formatted(line));
            System.out.println(matcher.group("licenseName"));
            System.out.println(matcher.group("projectName"));
            System.out.println(matcher.group("groupId"));
            System.out.println(matcher.group("artifactId"));
            System.out.println(matcher.group("version"));
            System.out.println(matcher.group("projectUrl"));
            System.out.println();

            return Optional.of(new Dependency(
                    matcher.group("licenseName"),
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
