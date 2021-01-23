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
    private static final Pattern URL = Pattern.compile("(?:(?:https?[: ]\\/\\/)?(?<content>(www)?(?:[a-z0-9-]{1,256}\\.)+(?:[a-z]{2,})(?:\\/[a-z0-9-_]+)*\\/?))", Pattern.CASE_INSENSITIVE);

    private final ReadOnlyListWrapper<String> urls = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    private final StringProperty twitchUrl = new SimpleStringProperty();
    private final BooleanProperty twitch = new SimpleBooleanProperty();

    public ReadOnlyListProperty<String> getUrls() {
        return urls.getReadOnlyProperty();
    }

    public String getTwitchUrl() {
        return twitchUrl.get();
    }

    public ReadOnlyStringProperty twitchUrlProperty() {
        return twitchUrl;
    }

    public boolean isTwitch() {
        return twitch.get();
    }

    public ReadOnlyBooleanProperty twitchProperty() {
        return twitch;
    }

    void setUrlsFromString(final String s) {
        urls.setAll(parseStrings(s));

        final Optional<String> maybeTwitchUrl = urls.stream().filter(e -> e.contains("twitch")).findFirst();

        twitchUrl.set(maybeTwitchUrl.orElse(null));
        twitch.set(maybeTwitchUrl.isPresent());
    }

    private static List<String> parseStrings(final String s) {
        if (s == null || s.isBlank()) {
            return Collections.emptyList();
        }

        return URL
                .matcher(s.replaceAll("/./\s+$", ""))
                .results()
                .map(e -> e.group(1))
                .map(e -> e.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
    }
}
