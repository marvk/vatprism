package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.collections.FXCollections;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Urls {
    private static final Pattern TWITCH_URL = Pattern.compile("(?<twitchWithSpace>twitch\\.tv[ /][A-Z0-9_]+)|(?:(?:https?[: ]\\/\\/)?(?<content>(www)?(?:[a-z0-9-]{1,256}\\.)+(?:[a-z]{2,})(?:\\/[a-z0-9-_]+)*\\/?))", Pattern.CASE_INSENSITIVE);
    private static final Pattern YOUTUBE_URL = Pattern.compile("(?<youtubeWithSpace>youtube\\.com[ /][A-Z0-9_]+)|(?:(?:https?[: ]\\/\\/)?(?<content>(www)?(?:[a-z0-9-]{1,256}\\.)+(?:[a-z]{2,})(?:\\/[a-z0-9-_@]+)*\\/?))", Pattern.CASE_INSENSITIVE);

    private final ReadOnlyListWrapper<String> urls = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    private final StringProperty url = new SimpleStringProperty();
    private final BooleanProperty livestream = new SimpleBooleanProperty();
    private final StringProperty platform = new SimpleStringProperty();

    public ReadOnlyListProperty<String> getUrls() {
        return urls.getReadOnlyProperty();
    }

    public String getUrl() {
        return url.get();
    }

    public ReadOnlyStringProperty urlProperty() {
        return url;
    }

    public boolean getLivestream() {
        return livestream.get();
    }

    public ReadOnlyBooleanProperty livestreamProperty() {
        return livestream;
    }

    public ReadOnlyStringProperty platformProperty() {
        return platform;
    }

    void setUrlsFromString(final String s) {
        urls.setAll(parseStrings(s));

        final Optional<String> maybeStreamUrl = urls.stream().filter(e -> e.contains("twitch")
                || e.contains("youtube")).findFirst();

        platform.set("Unknown");
        if (maybeStreamUrl.isPresent() && maybeStreamUrl.get().contains("youtube")) {
            platform.set("YouTube");
        } else {
            platform.set("Twitch.tv");
        }

        url.set(maybeStreamUrl.orElse(null));
        livestream.set(maybeStreamUrl.isPresent());
    }

    private static List<String> parseStrings(final String s) {
        if (s == null || s.isBlank()) {
            return Collections.emptyList();
        }

        if (s.toLowerCase(Locale.ROOT).contains("youtube.com/")) {
            return YOUTUBE_URL
                    .matcher(s.replaceAll("/./\s+$", ""))
                    .results()
                    .map(Urls::getGroup)
                    .map(e -> e.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toList());
        }
        return TWITCH_URL
                .matcher(s.replaceAll("/./\s+$", ""))
                .results()
                .map(Urls::getGroup)
                .map(e -> e.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
    }

    private static String getGroup(final java.util.regex.MatchResult e) {
        final String g1 = e.group(1);

        if (g1 == null || g1.isEmpty()) {
            return e.group(2);
        }

        if (g1.toLowerCase(Locale.ROOT).contains("youtube")) {
            return g1.replaceAll("(?i)youtube\\.com\s+", "youtube\\.com/");
        }
        return g1.replaceAll("(?i)twitch\\.tv\s+", "twitch\\.tv/");
    }
}
